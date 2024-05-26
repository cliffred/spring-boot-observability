package red.cliff.observability.customer

import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerEventRepository : CoroutineCrudRepository<CustomerEvent, String> {
    fun findByIdGreaterThan(offset: ObjectId): Flow<CustomerEvent>
}
