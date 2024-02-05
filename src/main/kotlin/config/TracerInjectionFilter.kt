package red.cliff.observability.config

import io.micrometer.tracing.Tracer
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.random.Random

private const val CUSTOM_TX_ID = "custom_tx_id"
private const val TRACE_ID_HEADER = "Trace-Id"

@Component
class TracerInjectionFilter(private val tracer: Tracer) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        tracer.createBaggageInScope(CUSTOM_TX_ID, Random.nextInt(100, 1000).toString())

        val currentUser = (SecurityContextHolder.getContext().authentication.principal as? User)?.username ?: "-"
        tracer.currentSpan()?.tag("username", currentUser)

        tracer.currentSpan()?.context()?.traceId()?.let { traceId ->
            response.addHeader(TRACE_ID_HEADER, traceId)
        }

        return filterChain.doFilter(request, response)
    }
}
