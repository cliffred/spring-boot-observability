package red.cliff.observability.client

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.service.annotation.GetExchange

interface HttpBinClient {
    @GetExchange("/get")
    fun get(): JsonNode
}
