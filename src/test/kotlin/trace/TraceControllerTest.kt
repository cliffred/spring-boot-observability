package red.cliff.observability.trace

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.types.shouldBeTypeOf
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import red.cliff.observability.Manual

@SpringBootTest
@AutoConfigureMockMvc
class TraceControllerTest(
    private val webTestClient: WebTestClient,
) : ShouldSpec(
        {
            should("return OK from info endpoint") {
                webTestClient
                    .get()
                    .uri("/trace/info")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<String>()
                    .isEqualTo("OK")
            }

            should("call external API and propagate trace id").config(tags = setOf(Manual)) {
                webTestClient
                    .get()
                    .uri("/trace/api-call")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<Map<String, *>>()
                    .consumeWith {
                        val traceparent = it.responseBody?.get("traceparentHeader")
                        traceparent.shouldBeTypeOf<String>()
                        traceparent.shouldHaveLength(55)
                    }
            }
        },
    )
