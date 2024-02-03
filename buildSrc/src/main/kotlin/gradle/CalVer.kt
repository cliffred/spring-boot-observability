package red.cliff.gradle

import java.time.LocalDateTime

data class CalVer(val year: Int, val month: Int, val revision: Int, val snapshot: Boolean = false) : Comparable<CalVer> {
    private val snapshotStr = if (snapshot) "-SNAPSHOT" else ""

    fun increment(pointInTime: LocalDateTime) =
        when {
            year == pointInTime.year && month == pointInTime.monthValue -> copy(revision = revision + 1)
            else -> invoke(pointInTime, 1)
        }

    fun snapshot() = copy(snapshot = true)

    fun release() = copy(snapshot = false)

    override fun compareTo(other: CalVer) =
        compareValuesBy(
            this,
            other,
            { it.year },
            { it.month },
            { it.revision },
            { !it.snapshot },
        )

    override fun toString() = "$year.${month.leftPad()}.${revision.leftPad()}$snapshotStr"

    private fun Int.leftPad() = toString().padStart(2, '0')

    companion object {
        operator fun invoke(
            pointInTime: LocalDateTime,
            revision: Int,
            snapshot: Boolean = false,
        ) = CalVer(
            year = pointInTime.year,
            month = pointInTime.monthValue,
            revision = revision,
            snapshot = snapshot,
        )

        fun parse(version: String) =
            runCatching {
                val parts = version.split('-')
                val rawValues = parts[0].split('.')

                require(parts.size <= 2 && rawValues.size == 3 && parts.getOrNull(1)?.let { it == "SNAPSHOT" } ?: true) {
                    "Value '$version' does not match versioning scheme."
                }

                CalVer(
                    year = rawValues[0].toInt(),
                    month = rawValues[1].toInt(),
                    revision = rawValues[2].toInt(),
                    snapshot = parts.getOrNull(1) != null,
                )
            }
    }
}
