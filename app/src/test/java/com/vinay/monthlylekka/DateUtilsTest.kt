package com.vinay.monthlylekka

import com.vinay.monthlylekka.ui.formatShortDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun formatShortDate_formatsLocalDateAsDdMmYy() {
        val date1 = LocalDate.of(2026, 9, 5)
        assertEquals("05/09/26", date1.formatShortDate())

        val date2 = LocalDate.of(2026, 1, 15)
        assertEquals("15/01/26", date2.formatShortDate())

        val date3 = LocalDate.of(2025, 12, 31)
        assertEquals("31/12/25", date3.formatShortDate())
    }
}
