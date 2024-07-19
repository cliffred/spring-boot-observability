package red.cliff.observability.config

import io.opentelemetry.api.trace.Span
import io.prometheus.metrics.tracer.common.SpanContext
import org.springframework.stereotype.Component

@Component
class OtelSpanContext : SpanContext {
    override fun getCurrentTraceId(): String = Span.current().spanContext.traceId

    override fun getCurrentSpanId(): String = Span.current().spanContext.spanId

    override fun isCurrentSpanSampled(): Boolean = Span.current().spanContext.isSampled

    override fun markCurrentSpanAsExemplar() {
        // Do nothing
    }
}
