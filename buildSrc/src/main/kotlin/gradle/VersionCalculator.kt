package gradle

import red.cliff.gradle.CalVer
import java.time.Clock
import java.time.LocalDateTime

class VersionCalculator(
    private val gitState: GitState,
    private val clock: Clock = Clock.systemUTC(),
) {
    fun calVer(): CalVer {
        val (currentVersion, clean, baseVersion) = gitState
        return when {
            currentVersion != null && clean -> currentVersion
            currentVersion != null && !clean -> currentVersion.increment(LocalDateTime.now(clock)).snapshot()
            baseVersion != null -> baseVersion.increment(LocalDateTime.now(clock)).snapshot()
            else -> CalVer(LocalDateTime.now(clock), 1).snapshot()
        }
    }
}
