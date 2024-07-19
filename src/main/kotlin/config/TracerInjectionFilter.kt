package red.cliff.observability.config

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.trace.Span
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import red.cliff.observability.auth.User
import kotlin.random.Random

private const val RANDOM_BAGGAGE = "baggage.random"
private const val TRACE_ID_HEADER = "Trace-Id"

@Component
class TracerInjectionFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        Baggage
            .builder()
            .put(RANDOM_BAGGAGE, Random.nextInt(100, 1000).toString())
            .build()
            .makeCurrent()

        val span = Span.current()
        val currentUser = (SecurityContextHolder.getContext().authentication.principal as? User)?.username ?: "unauthenticated"
        span.setAttribute("username", currentUser)
        response.addHeader(TRACE_ID_HEADER, span.spanContext.traceId)

        return filterChain.doFilter(request, response)
    }
}
