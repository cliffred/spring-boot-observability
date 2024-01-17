package red.cliff.observability

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.tracing.Tracer
import org.slf4j.LoggerFactory
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
        .baseUrl("http://httpbin.org")
        .build()

    @GetMapping("/hello")
    fun hello(): Map<*, *> {
        val response = client.get().uri("get").retrieve()
        val body: Map<String, Any> = requireNotNull(response.body())
        val requestHeaders = body["headers"]

        val span = tracer.currentSpan() ?: tracer.nextSpan()

        logger.info("headers: $requestHeaders")

        return mapOf(
            "requestHeaders" to requestHeaders,
            "traceId" to span.context().traceId(),
            "spanId" to span.context().spanId(),
            "traceBaggage" to tracer.allBaggage
        )
    }
}