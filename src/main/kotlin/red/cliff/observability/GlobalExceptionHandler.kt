package red.cliff.observability

import io.micrometer.tracing.Tracer
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler(private val tracer: Tracer) : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    fun handleUnknownException(ex: Exception, request: WebRequest): ProblemDetail {
        logger.error("Unhandled error: ${ex.message}", ex)
        return createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.localizedMessage, null, null, request)
    }

    override fun createProblemDetail(
        ex: java.lang.Exception,
        status: HttpStatusCode,
        defaultDetail: String,
        detailMessageCode: String?,
        detailMessageArguments: Array<out Any>?,
        request: WebRequest
    ): ProblemDetail {
        val problemDetail =
            super.createProblemDetail(ex, status, defaultDetail, detailMessageCode, detailMessageArguments, request)
        return problemDetail.apply {
            tracer.currentSpan()?.context()?.traceId()?.let {
                setProperty("trace-id", it)
            }
        }
    }
}