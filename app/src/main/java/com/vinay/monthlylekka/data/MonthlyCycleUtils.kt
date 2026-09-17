package com.vinay.monthlylekka.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthlyCycle(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val label: String
) {
    val monthYearLabel: String
        get() = startDate.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US))

    val shortRangeLabel: String
        get() = if (startDate.year == endDate.year) {
            "${startDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))} - ${endDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))}"
        } else {
            "${startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))} - ${endDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))}"
        }
}

sealed class CycleOption {
    abstract val label: String
    abstract val dropdownLabel: String

    data class Specific(val cycle: MonthlyCycle, val isCurrent: Boolean = false) : CycleOption() {
        override val label: String
            get() = if (isCurrent) "Current Cycle (${cycle.monthYearLabel})" else cycle.label

        override val dropdownLabel: String
            get() = if (isCurrent) {
                "📅 ${cycle.monthYearLabel} (Active Cycle)"
            } else {
                "📅 ${cycle.monthYearLabel} (${cycle.shortRangeLabel})"
            }
    }

    object AllTime : CycleOption() {
        override val label: String get() = "All Time (Full History)"
        override val dropdownLabel: String get() = "📅 All Time (Full History)"
    }

    fun matchesDate(date: LocalDate): Boolean {
        return when (this) {
            is Specific -> !date.isBefore(cycle.startDate) && !date.isAfter(cycle.endDate)
            is AllTime -> true
        }
    }
}

private val MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MMM d", Locale.US)

fun getMonthlyCycleForDate(date: LocalDate, startDay: Int): MonthlyCycle {
    val validStartDay = startDay.coerceIn(1, 28)
    val (startDate, endDate) = if (date.dayOfMonth >= validStartDay) {
        val start = date.withDayOfMonth(validStartDay)
        val end = date.plusMonths(1).withDayOfMonth(validStartDay).minusDays(1)
        Pair(start, end)
    } else {
        val start = date.minusMonths(1).withDayOfMonth(validStartDay)
        val end = date.withDayOfMonth(validStartDay).minusDays(1)
        Pair(start, end)
    }

    val startFormatted = startDate.format(MONTH_DAY_FORMATTER)
    val endFormatted = endDate.format(MONTH_DAY_FORMATTER)
    val label = if (startDate.year == endDate.year) {
        "$startFormatted - $endFormatted, ${endDate.year}"
    } else {
        "$startFormatted, ${startDate.year} - $endFormatted, ${endDate.year}"
    }

    return MonthlyCycle(
        startDate = startDate,
        endDate = endDate,
        label = label
    )
}

fun getPastMonthlyCycles(currentDate: LocalDate, startDay: Int, count: Int = 12): List<MonthlyCycle> {
    val cycles = mutableListOf<MonthlyCycle>()
    var dateToQuery = currentDate
    repeat(count.coerceAtLeast(1)) {
        val cycle = getMonthlyCycleForDate(dateToQuery, startDay)
        cycles.add(cycle)
        dateToQuery = cycle.startDate.minusDays(1)
    }
    return cycles
}

fun getAvailableCycleOptions(currentDate: LocalDate = LocalDate.now(), startDay: Int, count: Int = 12): List<CycleOption> {
    val cycles = getPastMonthlyCycles(currentDate, startDay, count)
    val options = mutableListOf<CycleOption>()
    cycles.forEachIndexed { index, cycle ->
        options.add(CycleOption.Specific(cycle = cycle, isCurrent = (index == 0)))
    }
    options.add(CycleOption.AllTime)
    return options
}
