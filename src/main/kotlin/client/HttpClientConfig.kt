package red.cliff.observability.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class HttpClientConfig(
    private val restClientBuilder: RestClient.Builder,
    @Value("\${service.httpclient.httpbin.url}") private val httpBinUrl: String
) {
    @Bean
    fun httpBinClient(): HttpBinClient {
        val client = restClientBuilder.baseUrl(httpBinUrl).build()
        val factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build()
        return factory.createClient()
    }
}
