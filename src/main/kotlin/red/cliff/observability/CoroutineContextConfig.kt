package red.cliff.observability

import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.ObservationRegistry
import java.lang.reflect.Method
import kotlinx.coroutines.Dispatchers
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.context.annotation.Configuration
import org.springframework.core.CoroutinesUtils
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod

@Configuration
class CoroutineContextConfig(private val observationRegistry: ObservationRegistry) : WebMvcRegistrations {
    override fun getRequestMappingHandlerAdapter(): RequestMappingHandlerAdapter {
        return object : RequestMappingHandlerAdapter() {
            override fun createInvocableHandlerMethod(handlerMethod: HandlerMethod): ServletInvocableHandlerMethod {
                return object : ServletInvocableHandlerMethod(handlerMethod) {
                    override fun invokeSuspendingFunction(method: Method, target: Any, args: Array<out Any>): Any {
                        return CoroutinesUtils.invokeSuspendingFunction(
                            Dispatchers.Unconfined + observationRegistry.asContextElement(),
                            method,
                            target,
                            *args
                        )
                    }
                }
            }
        }
    }
}