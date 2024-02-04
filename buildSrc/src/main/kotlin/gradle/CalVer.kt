package red.cliff.gradle

import java.time.LocalDate

private const val CORE_SEPARATOR = "."
private const val REVISION_SEPARATOR = "_"
private const val SUFFIX_SEPARATOR = "-"
private const val REVISION_START = 1
private const val SCHEME = "yyyy${CORE_SEPARATOR}MM${CORE_SEPARATOR}dd${REVISION_SEPARATOR}#"

data class CalVer(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val revision: Int,
    val type: Type = Type.FINAL,
) : Comparable<CalVer> {
    fun increment(date: LocalDate) =
        when {
            year == date.year && month == date.monthValue && dayOfMonth == date.dayOfMonth -> copy(revision = revision + 1)
            else -> invoke(date, REVISION_START)
        }

    fun snapshot() = copy(type = Type.SNAPSHOT)

    fun final() = copy(type = Type.FINAL)

    override fun compareTo(other: CalVer) =
        compareValuesBy(
            this,
            other,
            { it.year },
            { it.month },
            { it.dayOfMonth },
            { it.revision },
            { it.type.order },
        )

    override fun toString(): String {
        val suffix = if (type.suffix.isEmpty()) "" else SUFFIX_SEPARATOR + type.suffix
        return "${year}${CORE_SEPARATOR}${month.leftPad()}${CORE_SEPARATOR}${dayOfMonth.leftPad()}${REVISION_SEPARATOR}${revision}$suffix"
    }

    private fun Int.leftPad() = toString().padStart(2, '0')

    companion object {
        operator fun invoke(
            date: LocalDate,
            revision: Int = REVISION_START,
            type: Type = Type.FINAL,
        ) = CalVer(
            year = date.year,
            month = date.monthValue,
            dayOfMonth = date.dayOfMonth,
            revision = revision.also { require(revision >= REVISION_START) },
            type = type,
        )

        fun parse(version: String) =
            runCatching {
                val parts = version.split(SUFFIX_SEPARATOR)
                val (base, revision) = parts[0].split(REVISION_SEPARATOR)
                val core = base.split(CORE_SEPARATOR)

                require(parts.size <= 2 && core.size == 3)
                val date = LocalDate.parse("${core[0]}-${core[1]}-${core[2]}")
                val suffix = parts.getOrElse(1) { "" }
                val type = Type.values().first { it.suffix == suffix }
                CalVer(
                    year = date.year,
                    month = date.monthValue,
                    dayOfMonth = date.dayOfMonth,
                    revision = revision.toInt().also { require(it >= REVISION_START) },
                    type = type,
                )
            }.recoverCatching {
                throw IllegalArgumentException("Value '$version' does not match version scheme '$SCHEME'.")
            }
    }
}

enum class Type(val suffix: String, val order: Int) {
    SNAPSHOT("SNAPSHOT", 0),
    FINAL("", 1)
}
