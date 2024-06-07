package red.cliff.observability.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.tracing.Tracer
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeAttribute.ALWAYS
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler(
    private val tracer: Tracer,
    private val objectMapper: ObjectMapper,
    serverProperties: ServerProperties,
) : ResponseEntityExceptionHandler() {
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

    @ExceptionHandler(AuthenticationException::class, AccessDeniedException::class)
    fun handleSecurityException(
        request: WebRequest,
        response: HttpServletResponse,
        exception: Exception,
    ) {
        val problemDetail =
            when (exception) {
                is AuthenticationException ->
                    createProblemDetail(
                        ex = exception,
                        status = HttpStatus.UNAUTHORIZED,
                        defaultDetail = "Not authenticated",
                        detailMessageCode = null,
                        detailMessageArguments = null,
                        request = request,
                    )

                is AccessDeniedException ->
                    createProblemDetail(
                        ex = exception,
                        status = HttpStatus.FORBIDDEN,
                        defaultDetail = "Access denied",
                        detailMessageCode = null,
                        detailMessageArguments = null,
                        request = request,
                    )

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
            problemDetail.setProperty("message", ex.message)
        }

        if (includeStacktrace) {
            problemDetail.setProperty("trace", ex.stackTraceToString())
        }

        tracer.currentSpan()?.context()?.traceId()?.let {
            problemDetail.setProperty("trace-id", it)
        }

        return problemDetail
    }
}
