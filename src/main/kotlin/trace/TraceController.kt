package red.cliff.observability.trace

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.trace.Span
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpServerErrorException
import red.cliff.observability.client.HttpBinClient

//@RestController
//@RequestMapping("/trace")
class TraceController(
    private val httpBinClient: HttpBinClient,
    private val traceService: TraceService,
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/api-call")
    fun apiCall(): Map<*, *> {
        val response = httpBinClient.get()
        val responseHeaders = response["headers"]
        val traceparentHeader = responseHeaders["Traceparent"]?.asText() ?: "-"
        val spanContext = Span.current().spanContext

        logger.info { "The following headers were in the request: $responseHeaders" }
        logger.info { "Returning result with traceId ${spanContext.traceId}" }

        return mapOf(
            "traceparentHeader" to traceparentHeader,
            "traceId" to spanContext.traceId,
            "spanId" to spanContext.spanId,
            "traceBaggage" to Baggage.current().asMap().mapValues { it.value.value },
        )
    }

    @GetMapping("/error")
    fun error(): Map<*, *> = throw HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong")

    @GetMapping("/info")
    fun health(): String {
        logger.info { "Called info endpoint" }
        return "OK"
    }

    @GetMapping("/random")
    fun random(): String {
        logger.info { "Call to /random" }
        val random = traceService.generateRandomNumber(1, 10)
        Span.current().setAttribute("random.value", random)
        when (random) {
            in 1..5 -> logger.info { "Random info" }
            in 6..8 -> logger.warn { "Random warning" }
            in 9..10 -> logger.error { "Random error" }
        }
        return "Random value: $random"
    }

    @GetMapping("/suspend")
    suspend fun suspend(): String {
        logger.info { "Call to /suspend" }
        withContext(Dispatchers.IO) {
            logger.info { "From inside scope with IO Dispatcher" }
        }
        logger.info { "End /suspend" }
        return "OK"
    }
}
