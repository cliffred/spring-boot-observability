package red.cliff.observability

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.core.spec.style.ShouldSpec
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest(
    private val webTestClient: WebTestClient,
) : ShouldSpec(
        {
            should("return hello") {
                webTestClient
                    .get()
                    .uri("/trace")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<JsonNode>()
                    .consumeWith { println(it.responseBody?.toPrettyString()) }
            }
        },
    )
