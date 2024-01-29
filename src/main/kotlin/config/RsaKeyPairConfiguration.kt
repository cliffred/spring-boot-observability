package red.cliff.observability.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@ConfigurationProperties(prefix = "rsa")
data class RsaKeyProperties(val publicKey: RSAPublicKey? = null, val privateKey: RSAPrivateKey? = null)

data class RsaKeyPair(val publicKey: RSAPublicKey, val privateKey: RSAPrivateKey)

@Configuration
@EnableConfigurationProperties(RsaKeyProperties::class)
class RsaKeyPairConfiguration {
    private val logger = KotlinLogging.logger {}

    @Bean
    @ConditionalOnProperty(name = ["rsa.private-key", "rsa.public-key"])
    fun rsaKeyPairFromProperties(properties: RsaKeyProperties): RsaKeyPair {
        logger.debug { "Loading RSA key pair from config" }
        return RsaKeyPair(properties.publicKey!!, properties.privateKey!!)
    }

    @Bean
    @ConditionalOnProperty(name = ["rsa.private-key", "rsa.public-key"], havingValue = "false", matchIfMissing = true)
    fun generatedRsaKeyPair(): RsaKeyPair {
        logger.debug { "Generating new RSA key pair" }
        return KeyPairGenerator
            .getInstance("RSA")
            .apply { initialize(2048) }
            .genKeyPair()
            .let { RsaKeyPair(it.public as RSAPublicKey, it.private as RSAPrivateKey) }
    }
}
