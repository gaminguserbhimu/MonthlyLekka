package com.vinay.monthlylekka

import com.vinay.monthlylekka.ui.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyUtilsTest {

    @Test
    fun toCurrencyString_formatsStandardAmountsWithCommasAndRupeePrefix() {
        assertEquals("₹ 1,50,000", 150000.0.toCurrencyString())
        assertEquals("₹ 5,000", 5000.0.toCurrencyString())
        assertEquals("₹ 100", 100.0.toCurrencyString())
        assertEquals("₹ 0", 0.0.toCurrencyString())
    }

    @Test
    fun toCurrencyString_formatsNegativeAndLargeAmountsCorrectly() {
        assertEquals("-₹ 5,000", (-5000.0).toCurrencyString())
        assertEquals("₹ 12,34,567", 1234567.0.toCurrencyString())
    }

    @Test
    fun toCurrencyString_formatsDecimalsWhenPresent() {
        assertEquals("₹ 1,50,000.50", 150000.50.toCurrencyString())
    }
}
