package red.cliff.observability.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.context.request.ServletWebRequest

class BasicAuthenticationEntryPoint(
    private val realmName: String = "Realm",
    private val exceptionHandler: GlobalExceptionHandler,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.setHeader("WWW-Authenticate", "Basic realm=\"$realmName\"")
        exceptionHandler.handleSecurityException(ServletWebRequest(request), response, authException)
    }
}
