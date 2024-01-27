package red.cliff.observability

import io.micrometer.observation.ObservationRegistry
import io.micrometer.tracing.Tracer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@TestConfiguration
class TestConfig {
    @Bean
    fun observationRegistry(): ObservationRegistry = ObservationRegistry.NOOP

    @Bean
    fun tracer(): Tracer = Tracer.NOOP

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
        }
        return http.build()
    }
}
