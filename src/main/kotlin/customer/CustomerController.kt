package red.cliff.observability.customer

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Duration.Companion.seconds

private val HEART_BEAT_TIME = 5.seconds
private val HEART_BEAT_SSE = ServerSentEvent.builder<CustomerEvent>().event("heartbeat").build()

@OptIn(ExperimentalCoroutinesApi::class)
@RestController
@RequestMapping("/api")
class CustomerController(
    private val customerService: CustomerService,
    private val eventNotifier: EventNotifier,
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/customers")
    fun getCustomers(): Flow<Customer> {
        logger.info { "Getting all customers" }
        return customerService.getAllCustomers()
    }

    @PostMapping("/customers")
    suspend fun createCustomer(
        @RequestBody customer: Customer,
    ): Customer {
        logger.info { "Creating customer ${customer.email}" }
        return customerService.createCustomer(customer)
    }

    @GetMapping("/customers/events")
    fun getCustomerEvents(
        @RequestParam offset: String?
    ): Flow<ServerSentEvent<CustomerEvent?>> =
        flow {
            logger.info { "Streaming customer events with offset $offset" }
            var lastId = offset

            suspend fun fetch() {
                println("Fetching with offset $offset")
                customerService.getCustomerEvents(lastId).collect { event ->
                    val sse = event.toSse()
                    emit(sse)
                    lastId = event.id
                }
            }
            emit(HEART_BEAT_SSE)
            fetch()
            eventNotifier.subscribe().collect { fetch() }
        }.transformLatest {
            emit(it)
            while (true) {
                delay(HEART_BEAT_TIME)
                emit(HEART_BEAT_SSE)
            }
        }
}

private fun CustomerEvent.toSse() =
    ServerSentEvent
        .builder<CustomerEvent>(this)
        .id(id.toString())
        .event(type.toString())
        .build()
