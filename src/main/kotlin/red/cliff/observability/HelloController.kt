package red.cliff.observability

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.tracing.TraceContext
import io.micrometer.tracing.Tracer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@RestController
class HelloController(
    private val tracer: Tracer,
    rcb: RestClient.Builder,
) {

    private val logger = KotlinLogging.logger {}

    private val client = rcb
        .baseUrl("https://httpbin.org")
        .build()

    @GetMapping("/hello")
    fun hello(): Map<*, *> {
        val response = client.get().uri("get").retrieve()
        val body: JsonNode = requireNotNull(response.body())
        val traceparentHeader = body["headers"]["Traceparent"].asText()

        val traceContext = tracer.currentSpan()?.context() ?: TraceContext.NOOP

        logger.info { "Returning hello result with traceId ${traceContext.traceId()}" }

        return mapOf(
            "traceparentHeader" to traceparentHeader,
            "traceId" to traceContext.traceId(),
            "spanId" to traceContext.spanId(),
            "traceBaggage" to tracer.allBaggage
        )
    }
}