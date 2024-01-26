package red.cliff.observability.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import javax.sql.DataSource

@EnableWebSecurity
@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        securityExceptionHandler: SecurityExceptionHandler,
    ): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize("/api/**", authenticated)
                authorize("/actuator/**", hasRole("ADMIN"))
                authorize(anyRequest, permitAll)
            }
            httpBasic { }
            exceptionHandling {
                accessDeniedHandler = securityExceptionHandler
                authenticationEntryPoint = securityExceptionHandler
            }
        }
        return http.build()
    }

    @Bean
    fun dataSource(): DataSource {
        return EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript(JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION)
            .build()
    }

    @Bean
    fun users(dataSource: DataSource): UserDetailsManager {
        val user =
            User.withDefaultPasswordEncoder()
                .username("user")
                .password("user")
                .roles("USER")
                .build()
        val userManager = JdbcUserDetailsManager(dataSource)
        userManager.createUser(user)
        return userManager
    }
}
