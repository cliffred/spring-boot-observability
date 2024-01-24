package red.cliff.observability.customer

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.tracing.TraceContext
import io.micrometer.tracing.Tracer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import red.cliff.observability.client.HttpBinClient

@RestController
class CustomerController(
    private val customerRepository: CustomerRepository,
    private val tracer: Tracer,
    private val httpBinClient: HttpBinClient
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/trace")
    fun trace(): Map<*, *> {
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

    @GetMapping("/customers")
    fun getCustomers(): List<Customer> {
        logger.info { "Getting all customers" }
        return customerRepository.findAll()
    }

    @PostMapping("/customers")
    fun createCustomer(
        @RequestBody customer: Customer
    ): Customer {
        logger.info { "Creating customer ${customer.email}" }
        return customerRepository.save(customer)
    }
}
