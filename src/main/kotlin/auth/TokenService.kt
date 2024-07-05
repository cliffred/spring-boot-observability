package red.cliff.observability.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class TokenService(
    private val encoder: JwtEncoder,
    @Value("\${service.auth.token.validity}") private val validity: Duration,
) {
    fun createToken(authentication: Authentication): String {
        val authorities = authentication.authorities.joinToString(" ") { it.authority }

        val now = Instant.now()
        val validity = now.plus(validity)

        val claims =
            JwtClaimsSet
                .builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(authentication.name)
                .claim("scope", authorities)
                .build()

        return encoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }
}
