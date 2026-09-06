package com.vinay.monthlylekka.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")

/**
 * Formats a [LocalDate] into a compact short date string ("dd/MM/yy").
 * Example: 2026-09-05 -> "05/09/26"
 */
fun LocalDate.formatShortDate(): String {
    return this.format(SHORT_DATE_FORMATTER)
}
