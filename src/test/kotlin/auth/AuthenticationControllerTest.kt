package red.cliff.observability.auth

import io.kotest.core.spec.style.ShouldSpec
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import red.cliff.observability.IntegrationTest

@IntegrationTest
@TestPropertySource(
    properties = ["service.rsa.private-key=classpath:certs/private.pem", "service.rsa.public-key=classpath:certs/public.pem"]
)
class AuthenticationControllerTestRsaFromFile(private val webTestClient: WebTestClient) : ShouldSpec(
    {
        testAuthentication(webTestClient)
    },
)

@IntegrationTest
class AuthenticationControllerTestRsaGenerated(private val webTestClient: WebTestClient) : ShouldSpec(
    {
        testAuthentication(webTestClient)
    },
)

private fun ShouldSpec.testAuthentication(webTestClient: WebTestClient) {
    should("return 401 when not authenticated") {
        webTestClient
            .get()
            .uri("/api/test")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    should("return 200 when authenticated") {
        val token =
            webTestClient
                .post()
                .uri("/api/token")
                .headers { it.setBasicAuth("user", "user") }
                .exchange()
                .expectBody<String>()
                .returnResult()
                .responseBody

        webTestClient
            .get()
            .uri("/api/test")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus()
            .isOk
    }
}

@RestController
class TestController {
    @GetMapping("/api/test")
    fun test() = "Hello"
}
