package red.cliff.observability.config

import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import red.cliff.observability.auth.User
import red.cliff.observability.auth.UserRepository
import red.cliff.observability.customer.Customer
import red.cliff.observability.customer.CustomerEventRepository
import red.cliff.observability.customer.CustomerRepository
import red.cliff.observability.customer.CustomerService

@Configuration
class InsertTestData(
    private val userRepository: UserRepository,
    private val customerRepository: CustomerRepository,
    private val customerEventRepository: CustomerEventRepository,
    private val customerService: CustomerService,
    private val passwordEncoder: PasswordEncoder,
) {
    private val faker = Faker()

    @EventListener(ApplicationReadyEvent::class)
    fun testData(): Unit =
        runBlocking {
            clearData()
            testUsers()
            testCustomers()
        }

    private suspend fun clearData() {
        userRepository.deleteAll()
        customerRepository.deleteAll()
        customerEventRepository.deleteAll()
    }

    private suspend fun testUsers() {
        userRepository.save(
            User(
                username = "user",
                password = passwordEncoder.encode("user"),
                roles = setOf("USER"),
            ),
        )
        userRepository.save(
            User(
                username = "admin",
                password = passwordEncoder.encode("admin"),
                roles = setOf("USER", "ADMIN"),
            ),
        )
    }

    suspend fun testCustomers() {
        repeat(10) {
            customerService.createCustomer(
                Customer(
                    firstName = faker.name.firstName(),
                    lastName = faker.name.lastName(),
                    email = faker.internet.email(),
                ),
            )
        }
    }
}
