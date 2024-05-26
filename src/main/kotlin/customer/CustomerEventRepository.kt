package red.cliff.observability.customer

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerEventRepository : CoroutineCrudRepository<CustomerEvent, String>
