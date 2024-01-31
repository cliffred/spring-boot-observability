package red.cliff.observability.trace

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.tracing.annotation.NewSpan
import io.micrometer.tracing.annotation.SpanTag
import org.springframework.stereotype.Service

@Service
class TraceService {
    private val logger = KotlinLogging.logger { }

    @NewSpan
    fun generateRandomNumber(
        @SpanTag("from") from: Long,
        @SpanTag("to") toInclusive: Long,
    ): Long {
        val random = (from..toInclusive).random()
        logger.info { "Generated random number $random" }
        return random
    }
}
