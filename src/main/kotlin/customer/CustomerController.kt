package red.cliff.observability.customer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class CustomerController(
    private val customerRepository: CustomerRepository,
) {
    private val logger = KotlinLogging.logger {}

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
