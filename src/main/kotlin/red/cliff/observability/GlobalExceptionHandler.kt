package red.cliff.observability

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    fun handleUnknownException(ex: Exception, request: WebRequest): ProblemDetail {
        return createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.localizedMessage, null, null, request)
    }
}