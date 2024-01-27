package red.cliff.observability.config

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.boot.actuate.info.InfoEndpoint
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.HttpSecurityDsl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(private val exceptionHandler: GlobalExceptionHandler) {
    @Bean
    @Order(1)
    fun openActuatorEndpoint(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher(
                EndpointRequest.to(
                    HealthEndpoint::class.java,
                    InfoEndpoint::class.java,
                    PrometheusScrapeEndpoint::class.java,
                ),
            )
            authorizeRequests {
                authorize(anyRequest, permitAll)
            }
            sharedConfig()
        }
        return http.build()
    }

    @Bean
    @Order(2)
    fun securedActuatorEndpoint(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher(
                EndpointRequest.toAnyEndpoint(),
            )
            authorizeRequests {
                authorize(anyRequest, hasRole("ADMIN"))
            }
            sharedConfig()
        }
        return http.build()
    }

    @Bean
    @Order(3)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize("/api/**", authenticated)
                authorize(anyRequest, permitAll)
            }
            sharedConfig()
        }
        return http.build()
    }

    private fun HttpSecurityDsl.sharedConfig() {
        httpBasic {
            authenticationEntryPoint = exceptionHandler
        }
        exceptionHandling {
            accessDeniedHandler = exceptionHandler
            authenticationEntryPoint = exceptionHandler
        }
        csrf { disable() }
    }

    @Bean
    fun users(dataSource: DataSource): UserDetailsManager {
        val user =
            User.withDefaultPasswordEncoder()
                .username("user")
                .password("user")
                .roles("USER")
                .build()
        val admin =
            User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin")
                .roles("ADMIN", "USER")
                .build()
        return JdbcUserDetailsManager(dataSource).apply {
            createUser(user)
            createUser(admin)
        }
    }
}
