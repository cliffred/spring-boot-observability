package red.cliff.observability.customer

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import red.cliff.observability.TestConfig
import red.cliff.observability.withMockUser

@WebMvcTest(CustomerController::class)
@Import(TestConfig::class)
class CustomerControllerTest(
    @MockkBean private val customerService: CustomerService,
    @MockkBean private val eventNotifier: EventNotifier,
    private val webTestClient: WebTestClient,
) : ShouldSpec(
        {
            should("return all customers") {
                withMockUser()
                val customer =
                    Customer(
                        id = "1",
                        firstName = "John",
                        lastName = "Doe",
                        email = "j.doe@example.org",
                    )
                every { customerService.getAllCustomers() } returns flowOf(customer)

                webTestClient
                    .get()
                    .uri("/api/customers")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<Customer>()
                    .hasSize(1)
                    .contains(customer)
            }

            should("add new user") {
                withMockUser()
                val customer =
                    Customer(
                        id = "1",
                        firstName = "John",
                        lastName = "Doe",
                        email = "j.doe@example.org",
                    )

                coEvery { customerService.createCustomer(any()) } answers { firstArg() }

                webTestClient
                    .post()
                    .uri("/api/customers")
                    .bodyValue(customer)
                    .exchange()
                    .expectStatus().isOk

                coVerify { customerService.createCustomer(customer) }
            }
        },
    )
