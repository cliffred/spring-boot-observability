package red.cliff.observability.client

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class HttpClientConfig(private val restClientBuilder: RestClient.Builder) {
    @Bean
    fun httpBinClient(): HttpBinClient {
        val client = restClientBuilder.baseUrl("https://httpbin.org").build()
        val factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build()
        return factory.createClient()
    }
}
