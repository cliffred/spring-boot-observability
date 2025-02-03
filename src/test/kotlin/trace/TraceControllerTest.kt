package red.cliff.observability.trace

import com.github.tomakehurst.wiremock.WireMockServer
import com.marcinziolo.kotlin.wiremock.equalTo
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.returnsJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.maps.shouldHaveKeys
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.types.shouldBeTypeOf
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import red.cliff.observability.IntegrationTest
import java.util.UUID

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

            should("call httpBin API and propagate trace id") {
                val traceId = UUID.randomUUID().toString().replace("-", "")
                val parentId = traceId.take(16).reversed()
                val traceParent = "00-$traceId-$parentId-01"

                wiremock.get {
                    url equalTo "/get"
                } returnsJson {
                    body = """
                         {
                          "headers": {
                            "Traceparent": "$traceParent"
                          }
                        }
                          """
                }

                webTestClient
                    .get()
                    .uri("/trace/api-call")
                    .header("traceparent", traceParent)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<Map<String, *>>()
                    .value {
                        it.shouldHaveKeys("traceparentHeader")
                        val traceParentHeader = it["traceparentHeader"]
                        traceParentHeader.shouldBeTypeOf<String>()
                        traceParentHeader shouldHaveLength 55
                        traceParentHeader shouldContain traceId
                    }
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
                    .isEqualTo("500 Something went wrong with the request")
            }
        },
    )
