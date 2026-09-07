package com.vinay.monthlylekka.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthlyCycle(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val label: String
)

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
