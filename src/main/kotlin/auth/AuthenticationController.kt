package red.cliff.observability.auth

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuthenticationController(private val tokenService: TokenService) {
    @PostMapping("/token")
    fun login(authentication: Authentication): String {
        val token = tokenService.createToken(authentication)
        return token
    }
}
