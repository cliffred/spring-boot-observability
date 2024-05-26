package red.cliff.observability.customer

import org.springframework.data.annotation.Id

data class CustomerEvent(
    @Id val id: String? = null,
    val type: EventType,
    val payload: Customer,
)

enum class EventType {
    CREATED,
    DELETED,
}
