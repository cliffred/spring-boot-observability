package red.cliff.observability

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.test.context.TestSecurityContextHolder

fun withMockUser(
    userName: String = "user",
    password: String = "password",
    roles: List<String> = listOf("USER"),
    authorities: List<String> = emptyList(),
) {
    require(!(authorities.isNotEmpty() && roles.isNotEmpty())) { "Supply authorities or roles, not both" }
    val grantedAuthorities = mutableListOf<GrantedAuthority>()
    authorities.forEach {
        grantedAuthorities += SimpleGrantedAuthority(it)
    }
    roles.forEach {
        require(!it.startsWith("ROLE_")) { "roles cannot start with ROLE_ Got $it" }
        grantedAuthorities += SimpleGrantedAuthority("ROLE_$it")
    }
    val principal = User(userName, password, true, true, true, true, grantedAuthorities)
    val authentication: Authentication =
        UsernamePasswordAuthenticationToken.authenticated(
            principal,
            principal.password,
            principal.authorities,
        )
    val context: SecurityContext = SecurityContextHolder.getContextHolderStrategy().createEmptyContext()
    context.authentication = authentication

    TestSecurityContextHolder.setContext(context)
}
