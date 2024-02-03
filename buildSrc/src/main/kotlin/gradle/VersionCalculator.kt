package gradle

import red.cliff.gradle.CalVer
import java.time.Clock
import java.time.LocalDate

class VersionCalculator(
    private val gitState: GitState,
    private val clock: Clock = Clock.systemUTC(),
) {
    fun calVer(): CalVer {
        val (currentVersion, clean, baseVersion) = gitState
        return when {
            currentVersion != null && clean -> currentVersion
            currentVersion != null && !clean -> currentVersion.increment(LocalDate.now(clock)).snapshot()
            baseVersion != null -> baseVersion.increment(LocalDate.now(clock)).snapshot()
            else -> CalVer(LocalDate.now(clock)).snapshot()
        }
    }
}
