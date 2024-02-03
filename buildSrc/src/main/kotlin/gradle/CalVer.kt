package red.cliff.gradle

import java.time.LocalDate

private const val CORE_SEPARATOR = "."
private const val REVISION_SEPARATOR = "_"
private const val SNAPSHOT_SEPARATOR = "-"
private const val REVISION_START = 1
private const val SCHEME = "yyyy${CORE_SEPARATOR}MM${CORE_SEPARATOR}dd${REVISION_SEPARATOR}#"

data class CalVer(val year: Int, val month: Int, val dayOfMonth: Int, val revision: Int, val snapshot: Boolean = false) :
    Comparable<CalVer> {
    private val snapshotSuffix = if (snapshot) "${SNAPSHOT_SEPARATOR}SNAPSHOT" else ""

    fun increment(date: LocalDate) =
        when {
            year == date.year && month == date.monthValue && dayOfMonth == date.dayOfMonth -> copy(revision = revision + 1)
            else -> invoke(date, REVISION_START)
        }

    fun snapshot() = copy(snapshot = true)

    fun release() = copy(snapshot = false)

    override fun compareTo(other: CalVer) =
        compareValuesBy(
            this,
            other,
            { it.year },
            { it.month },
            { it.dayOfMonth },
            { it.revision },
            { !it.snapshot },
        )

    override fun toString() =
        "${year}${CORE_SEPARATOR}${month.leftPad()}${CORE_SEPARATOR}${dayOfMonth.leftPad()}${REVISION_SEPARATOR}${revision}$snapshotSuffix"

    private fun Int.leftPad() = toString().padStart(2, '0')

    companion object {
        operator fun invoke(
            date: LocalDate,
            revision: Int = REVISION_START,
            snapshot: Boolean = false,
        ) = CalVer(
            year = date.year,
            month = date.monthValue,
            dayOfMonth = date.dayOfMonth,
            revision = revision.also { require(revision >= REVISION_START) },
            snapshot = snapshot,
        )

        fun parse(version: String) =
            runCatching {
                val parts = version.split(SNAPSHOT_SEPARATOR)
                val (base, revision) = parts[0].split(REVISION_SEPARATOR)
                val core = base.split(CORE_SEPARATOR)

                require(parts.size <= 2 && core.size == 3 && parts.getOrNull(1)?.let { it == "SNAPSHOT" } ?: true)
                val date = LocalDate.parse("${core[0]}-${core[1]}-${core[2]}")
                CalVer(
                    year = date.year,
                    month = date.monthValue,
                    dayOfMonth = date.dayOfMonth,
                    revision = revision.toInt().also { require(it >= REVISION_START) },
                    snapshot = parts.getOrNull(1) != null,
                )
            }.recoverCatching {
                throw IllegalArgumentException("Value '$version' does not match version scheme '$SCHEME'.")
            }
    }
}
