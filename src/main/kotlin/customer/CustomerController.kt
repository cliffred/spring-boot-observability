package red.cliff.observability.customer

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class CustomerController(
    private val customerService: CustomerService,
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
}
