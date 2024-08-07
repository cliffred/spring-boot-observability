package red.cliff.observability.customer

import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val eventRepository: CustomerEventRepository,
) {
    fun getAllCustomers(): Flow<Customer> = customerRepository.findAll()

    @Transactional
    suspend fun createCustomer(customer: Customer): Customer =
        customerRepository.save(customer).also { eventRepository.save(CustomerEvent(type = EventType.CREATED, payload = it)) }

    fun getCustomerEvents(offset: String?): Flow<CustomerEvent> {
        val objectId = offset?.let { ObjectId(it) } ?: ObjectId("0".repeat(24))
        return eventRepository.findByIdGreaterThan(objectId)
    }
}
