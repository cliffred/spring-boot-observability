package red.cliff.observability.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import kotlinx.coroutines.runBlocking
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
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import red.cliff.observability.auth.UserRepository

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val exceptionHandler: GlobalExceptionHandler,
    private val keyPair: RsaKeyPair,
    private val userRepository: UserRepository,
) {
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
        val entryPoint = BasicAuthenticationEntryPoint(exceptionHandler = exceptionHandler)
        httpBasic {
            authenticationEntryPoint = entryPoint
        }
        exceptionHandling {
            accessDeniedHandler = exceptionHandler
            authenticationEntryPoint = entryPoint
        }
        sessionManagement {
            sessionCreationPolicy = SessionCreationPolicy.STATELESS
        }
        csrf { disable() }
        oauth2ResourceServer { jwt { } }
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder
            .withPublicKey(keyPair.publicKey)
            .build()
    }

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val rsaKey =
            RSAKey.Builder(keyPair.publicKey)
                .privateKey(keyPair.privateKey)
                .build()
        val jwkSet = ImmutableJWKSet<SecurityContext>(JWKSet(rsaKey))
        return NimbusJwtEncoder(jwkSet)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(encoder: PasswordEncoder): UserDetailsService =
        UserDetailsService { username ->
            runBlocking {
                userRepository.findByUsername(username) ?: throw UsernameNotFoundException("User not found: $username")
            }
        }
}
