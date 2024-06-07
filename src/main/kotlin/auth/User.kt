package red.cliff.observability.auth

import org.springframework.data.annotation.Id
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class User(
    @Id
    val id: String? = null,
    private val username: String,
    private val password: String,
    val roles: Set<String>,
) : UserDetails {
    override fun getAuthorities() = roles.map { SimpleGrantedAuthority("ROLE_$it") }

    override fun getUsername() = username

    override fun getPassword() = password
}
