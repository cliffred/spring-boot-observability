package red.cliff.observability.customer

import org.springframework.data.annotation.Id

data class Customer(
    @Id val id: Long? = null,
    val firstName: String,
    val lastName: String,
    var email: String,
)
