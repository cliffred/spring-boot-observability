package red.cliff.observability.trace

import io.kotest.core.spec.style.ShouldSpec
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

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
        },
    )
