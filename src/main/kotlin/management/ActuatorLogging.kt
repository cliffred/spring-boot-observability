package management

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.Charset
import java.time.LocalTime

class LogRequestFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val cachedResponse = ContentCachingResponseWrapper(response)

        filterChain.doFilter(request, cachedResponse)

        val content = cachedResponse.contentAsByteArray
        val responseBody = String(content, Charset.forName("UTF-8"))

        val logbackMetrics = responseBody.lines().filter { "logback_events_total" in it }.joinToString("\n")

        val requestHeaders =
            request.headerNames.toList()
                .joinToString("\n") { "$it : ${request.getHeader(it)}" }

        logger.info(
            """
            |Incoming request to ${request.requestURI} from ${request.remoteAddr} at ${LocalTime.now()}
            |---------------------------------------
            |$requestHeaders
            |
            |RESPONSE:
            |$logbackMetrics
            |---------------------------------------
            """.trimMargin(),
        )

        cachedResponse.copyBodyToResponse()
    }
}

@ManagementContextConfiguration
class ActuatorConfig {
    @Bean
    fun logRequestFilter(): FilterRegistrationBean<LogRequestFilter> {
        return FilterRegistrationBean(LogRequestFilter()).apply { addUrlPatterns("/actuator/prometheus") }
    }
}
