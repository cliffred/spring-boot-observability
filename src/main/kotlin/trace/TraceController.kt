package red.cliff.observability.trace

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.tracing.TraceContext
import io.micrometer.tracing.Tracer
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpServerErrorException
import red.cliff.observability.client.HttpBinClient

@RestController
@RequestMapping("/trace")
class TraceController(
    private val tracer: Tracer,
    private val httpBinClient: HttpBinClient,
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/api-call")
    fun apiCall(): Map<*, *> {
        val response = httpBinClient.get()
        val traceparentHeader = response["headers"]["Traceparent"]?.asText() ?: "-"

        val traceContext = tracer.currentSpan()?.context() ?: TraceContext.NOOP

        logger.info { "Returning result with traceId ${traceContext.traceId()}" }

        return mapOf(
            "traceparentHeader" to traceparentHeader,
            "traceId" to traceContext.traceId(),
            "spanId" to traceContext.spanId(),
            "traceBaggage" to tracer.allBaggage,
        )
    }

    @GetMapping("/error")
    fun error(): Map<*, *> {
        throw HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong")
    }

    @GetMapping("/info")
    fun health(): String {
        logger.info { "Called info endpoint" }
        return "OK"
    }

    @GetMapping("/random")
    fun random(): Int {
        val random = (1..10).random()
        tracer.currentSpan()?.tag("random.value", random.toLong())
        when (random) {
            in 1..5 -> logger.info { "Random info" }
            in 6..8 -> logger.warn { "Random warning" }
            in 9..10 -> logger.error { "Random error" }
        }
        return random
    }
}
