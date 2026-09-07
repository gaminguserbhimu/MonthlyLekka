package com.vinay.monthlylekka

import com.vinay.monthlylekka.data.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun userPreferences_defaultMonthStartDayIs1() {
        val userPrefs = UserPreferences(context = null)
        assertEquals(1, userPrefs.getMonthStartDay())
        assertEquals(1, userPrefs.monthStartDay.value)
    }

    @Test
    fun setMonthStartDay_updatesValueAndCoercesToValidRange() {
        val userPrefs = UserPreferences(context = null)

        userPrefs.setMonthStartDay(5)
        assertEquals(5, userPrefs.getMonthStartDay())
        assertEquals(5, userPrefs.monthStartDay.value)

        // Coerce lower bound (0 -> 1)
        userPrefs.setMonthStartDay(0)
        assertEquals(1, userPrefs.getMonthStartDay())

        // Coerce upper bound (31 -> 28)
        userPrefs.setMonthStartDay(31)
        assertEquals(28, userPrefs.getMonthStartDay())
    }
}
