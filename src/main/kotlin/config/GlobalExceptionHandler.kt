package red.cliff.observability.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.tracing.Tracer
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeAttribute.ALWAYS
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.io.PrintWriter
import java.io.StringWriter

@ControllerAdvice
class GlobalExceptionHandler(
    private val tracer: Tracer,
    private val objectMapper: ObjectMapper,
    serverProperties: ServerProperties,
) : ResponseEntityExceptionHandler(), AuthenticationEntryPoint, AccessDeniedHandler {
    private val includeExceptionMessage = serverProperties.error.includeMessage == ALWAYS
    private val includeStacktrace = serverProperties.error.includeStacktrace == ALWAYS

    @ExceptionHandler(Exception::class)
    fun handleUnknownException(
        ex: Exception,
        request: WebRequest,
    ): ProblemDetail {
        logger.error("Unhandled error: ${ex.message}", ex)
        return createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error", null, null, request)
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) = handleSecurityException(ServletWebRequest(request), response, authException)

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) = handleSecurityException(ServletWebRequest(request), response, accessDeniedException)

    private fun handleSecurityException(
        request: WebRequest,
        response: HttpServletResponse,
        exception: Exception,
    ) {
        val problemDetail =
            when (exception) {
                is AuthenticationException ->
                    createProblemDetail(
                        exception,
                        HttpStatus.UNAUTHORIZED,
                        "Not authenticated",
                        null,
                        null,
                        request,
                    )

                is AccessDeniedException -> createProblemDetail(exception, HttpStatus.FORBIDDEN, "Access denied", null, null, request)
                else -> handleUnknownException(exception, request)
            }
        logger.debug { "No access: ${exception.message}" }
        response.writeProblem(problemDetail)
    }

    private fun HttpServletResponse.writeProblem(problemDetail: ProblemDetail) {
        status = problemDetail.status
        contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        objectMapper.writeValue(writer, problemDetail)
    }

    override fun createProblemDetail(
        ex: Exception,
        status: HttpStatusCode,
        defaultDetail: String,
        detailMessageCode: String?,
        detailMessageArguments: Array<out Any>?,
        request: WebRequest,
    ): ProblemDetail {
        val problemDetail =
            super.createProblemDetail(ex, status, defaultDetail, detailMessageCode, detailMessageArguments, request)

        if (includeExceptionMessage) {
            problemDetail.setProperty("message", ex.localizedMessage)
        }

        if (includeStacktrace) {
            val stackTrace = StringWriter()
            ex.printStackTrace(PrintWriter(stackTrace))
            stackTrace.flush()

            problemDetail.setProperty("trace", stackTrace.toString())
        }

        tracer.currentSpan()?.context()?.traceId()?.let {
            problemDetail.setProperty("trace-id", it)
        }

        return problemDetail
    }
}
