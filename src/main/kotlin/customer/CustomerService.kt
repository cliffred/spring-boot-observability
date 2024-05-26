package red.cliff.observability.customer

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val eventRepository: CustomerEventRepository,
) {

    fun getAllCustomers(): Flow<Customer> {
        return customerRepository.findAll()
    }

    @Transactional
    suspend fun createCustomer(customer: Customer): Customer {
        return customerRepository.save(customer).also { eventRepository.save(CustomerEvent(type = EventType.CREATED, payload = it)) }
    }
}
