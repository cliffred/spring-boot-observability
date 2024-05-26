package red.cliff.observability.auth

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CoroutineCrudRepository<User, String> {
    suspend fun findByUsername(username: String): User?
}
