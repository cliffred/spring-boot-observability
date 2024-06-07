package red.cliff.observability.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

/**
 * A compound [AuthenticationEntryPoint], [AuthenticationFailureHandler] and [AccessDeniedHandler]
 * that delegates exceptions to Spring WebMVC's [HandlerExceptionResolver] as defined in [WebMvcConfigurationSupport].
 */
@Component
class SecurityProblemSupport(
    @Qualifier("handlerExceptionResolver")
    private val resolver: HandlerExceptionResolver,
    private val realmName: String = "Realm",
) :
    AuthenticationEntryPoint, AuthenticationFailureHandler, AccessDeniedHandler {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        response.setHeader("WWW-Authenticate", "Basic realm=\"$realmName\"")
        resolver.resolveException(request, response, null, exception)
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        resolver.resolveException(request, response, null, exception)
    }

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AccessDeniedException
    ) {
        resolver.resolveException(request, response, null, exception)
    }
}
