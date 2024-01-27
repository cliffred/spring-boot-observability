package red.cliff.observability

import io.kotest.core.Tag
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

class KotestConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension)

    override val globalAssertSoftly = true
}

object Manual : Tag()
