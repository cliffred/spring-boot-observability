package red.cliff.observability.trace

import com.github.tomakehurst.wiremock.WireMockServer
import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.get
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.ints.shouldBeInRange
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import red.cliff.observability.IntegrationTest

@IntegrationTest
@AutoConfigureWireMock(port = 0)
class TraceControllerTest(
    private val webTestClient: WebTestClient,
    private val wiremock: WireMockServer,
) : ShouldSpec(
        {
            should("return OK from info endpoint") {
                webTestClient
                    .get()
                    .uri("/trace/info")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<String>()
                    .isEqualTo("OK")
            }

            should("return random number") {
                webTestClient
                    .get()
                    .uri("/trace/random")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<String>()
                    .value {
                        val number = it.substringAfterLast(" ").toInt()
                        number shouldBeInRange 1..10
                    }
            }

            should("return trace-id and message on error") {
                webTestClient
                    .get()
                    .uri("/trace/error")
                    .exchange()
                    .expectStatus()
                    .is5xxServerError
                    .expectBody()
                    .jsonPath("$.trace-id")
                    .exists()
                    .jsonPath("$.message")
                    .isEqualTo("500 Something went wrong")
            }
        },
    )
