package red.cliff.observability.trace

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.stereotype.Service

@Service
class TraceService {
    private val logger = KotlinLogging.logger { }

    @WithSpan
    fun generateRandomNumber(
        @SpanAttribute from: Long,
        @SpanAttribute toInclusive: Long,
    ): Long {
        val random = (from..toInclusive).random()
        logger.info { "Generated random number $random" }
        return random
    }
}
