package red.cliff.observability.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class SecurityExceptionHandler(private val objectMapper: ObjectMapper) : AuthenticationEntryPoint, AccessDeniedHandler {
    private val logger = KotlinLogging.logger {}

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) = handle(response, authException)

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) = handle(response, accessDeniedException)

    private fun handle(
        response: HttpServletResponse,
        exception: Exception,
    ) {
        val problemDetail =
            when (exception) {
                is AuthenticationException -> ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Not authenticated")
                is AccessDeniedException -> ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied")
                else -> throw exception
            }
        logger.debug { "No access: ${exception.message}" }
        response.writeProblem(problemDetail)
    }

    private fun HttpServletResponse.writeProblem(problemDetail: ProblemDetail) {
        status = problemDetail.status
        contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        objectMapper.writeValue(writer, problemDetail)
    }
}
