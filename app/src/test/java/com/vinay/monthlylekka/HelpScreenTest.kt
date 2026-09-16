package com.vinay.monthlylekka

import org.junit.Assert.assertTrue
import org.junit.Test

class HelpScreenTest {

    @Test
    fun helpScreen_containsFilterAndDateRangeTipText() {
        val tipTitle = "💡 Filter & Date Range Tip"
        val tipText = "📅 Date Range Filtering: By default, the Filter tab displays today's transactions. You can tap 'From Date' and 'To Date' anytime to select your own custom date range."

        assertTrue(tipTitle.contains("Filter & Date Range Tip"))
        assertTrue(tipText.contains("By default, the Filter tab displays today's transactions"))
        assertTrue(tipText.contains("select your own custom date range"))
    }
}
