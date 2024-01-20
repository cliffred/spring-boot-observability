package red.cliff.observability.config

import io.micrometer.tracing.Tracer
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.stereotype.Component
import kotlin.random.Random

private const val CUSTOM_TX_ID = "custom_tx_id"

@Component
class InjectCustomTxIdInBaggageFilter(private val tracer: Tracer) : Filter {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        tracer.createBaggageInScope(CUSTOM_TX_ID, Random.nextInt(100, 1000).toString())
        return chain.doFilter(request, response)
    }
}
