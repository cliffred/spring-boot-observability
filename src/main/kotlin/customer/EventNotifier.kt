package red.cliff.observability.customer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class EventNotifier(private val customerService: CustomerService) {
    private val eventNotifier = MutableSharedFlow<Unit>()
    private var offset: String = "0".repeat(24)

    fun subscribe(): Flow<Unit> = eventNotifier

    @Scheduled(fixedRate = 1000)
    fun pollEvents(): Unit =
        runBlocking {
            customerService.getCustomerEvents(offset).toList().takeIf { it.isNotEmpty() }?.let {
                eventNotifier.emit(Unit)
                offset = it.last().id!!
            }
        }
}
