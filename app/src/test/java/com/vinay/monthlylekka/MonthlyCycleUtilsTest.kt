package com.vinay.monthlylekka

import com.vinay.monthlylekka.data.CycleOption
import com.vinay.monthlylekka.data.getAvailableCycleOptions
import com.vinay.monthlylekka.data.getMonthlyCycleForDate
import com.vinay.monthlylekka.data.getPastMonthlyCycles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MonthlyCycleUtilsTest {

    @Test
    fun getMonthlyCycleForDate_whenDayIsGreaterOrEqualToStartDay_runsFromCurrentMonthToNextMonth() {
        // Date: 5th Sep 2026, Start Day: 5
        val date = LocalDate.of(2026, 9, 5)
        val cycle = getMonthlyCycleForDate(date, 5)

        assertEquals(LocalDate.of(2026, 9, 5), cycle.startDate)
        assertEquals(LocalDate.of(2026, 10, 4), cycle.endDate)
        assertEquals("Sep 5 - Oct 4, 2026", cycle.label)
    }

    @Test
    fun getMonthlyCycleForDate_whenDayIsLessThanStartDay_runsFromPreviousMonthToCurrentMonth() {
        // Date: 2nd Sep 2026, Start Day: 5
        val date = LocalDate.of(2026, 9, 2)
        val cycle = getMonthlyCycleForDate(date, 5)

        assertEquals(LocalDate.of(2026, 8, 5), cycle.startDate)
        assertEquals(LocalDate.of(2026, 9, 4), cycle.endDate)
        assertEquals("Aug 5 - Sep 4, 2026", cycle.label)
    }

    @Test
    fun getMonthlyCycleForDate_whenStartDayIs1_runsFrom1stToLastDayOfMonth() {
        val date = LocalDate.of(2026, 9, 10)
        val cycle = getMonthlyCycleForDate(date, 1)

        assertEquals(LocalDate.of(2026, 9, 1), cycle.startDate)
        assertEquals(LocalDate.of(2026, 9, 30), cycle.endDate)
        assertEquals("Sep 1 - Sep 30, 2026", cycle.label)
    }

    @Test
    fun getMonthlyCycleForDate_whenSpanningYearBoundary_formatsBothYearsInLabel() {
        val date = LocalDate.of(2026, 12, 10)
        val cycle = getMonthlyCycleForDate(date, 5)

        assertEquals(LocalDate.of(2026, 12, 5), cycle.startDate)
        assertEquals(LocalDate.of(2027, 1, 4), cycle.endDate)
        assertEquals("Dec 5, 2026 - Jan 4, 2027", cycle.label)
    }

    @Test
    fun getPastMonthlyCycles_returnsRequestedCountOfSequentialPastCycles() {
        val currentDate = LocalDate.of(2026, 9, 10)
        val pastCycles = getPastMonthlyCycles(currentDate, startDay = 5, count = 3)

        assertEquals(3, pastCycles.size)
        
        // 1st cycle: Sep 5 - Oct 4, 2026
        assertEquals(LocalDate.of(2026, 9, 5), pastCycles[0].startDate)
        assertEquals(LocalDate.of(2026, 10, 4), pastCycles[0].endDate)

        // 2nd cycle: Aug 5 - Sep 4, 2026
        assertEquals(LocalDate.of(2026, 8, 5), pastCycles[1].startDate)
        assertEquals(LocalDate.of(2026, 9, 4), pastCycles[1].endDate)

        // 3rd cycle: Jul 5 - Aug 4, 2026
        assertEquals(LocalDate.of(2026, 7, 5), pastCycles[2].startDate)
        assertEquals(LocalDate.of(2026, 8, 4), pastCycles[2].endDate)
    }

    @Test
    fun getAvailableCycleOptions_returnsCurrentCyclePastCyclesAndAllTime() {
        val currentDate = LocalDate.of(2026, 10, 10)
        val options = getAvailableCycleOptions(currentDate, startDay = 5, count = 3)

        assertEquals(4, options.size)

        // 1st option: Active cycle
        val currentOption = options[0] as CycleOption.Specific
        assertTrue(currentOption.isCurrent)
        assertEquals("📅 Oct 2026 (Active Cycle)", currentOption.dropdownLabel)

        // 2nd option: Past cycle
        val pastOption1 = options[1] as CycleOption.Specific
        assertFalse(pastOption1.isCurrent)
        assertEquals("📅 Sep 2026 (Sep 5 - Oct 4)", pastOption1.dropdownLabel)

        // 3rd option: Past cycle
        val pastOption2 = options[2] as CycleOption.Specific
        assertFalse(pastOption2.isCurrent)
        assertEquals("📅 Aug 2026 (Aug 5 - Sep 4)", pastOption2.dropdownLabel)

        // 4th option: All Time
        val allTimeOption = options[3]
        assertTrue(allTimeOption is CycleOption.AllTime)
        assertEquals("📅 All Time (Full History)", allTimeOption.dropdownLabel)
    }

    @Test
    fun cycleOption_matchesDate_correctlyFiltersDates() {
        val cycle = getMonthlyCycleForDate(LocalDate.of(2026, 9, 10), startDay = 5) // Sep 5 - Oct 4, 2026
        val specificOption = CycleOption.Specific(cycle, isCurrent = false)

        assertTrue(specificOption.matchesDate(LocalDate.of(2026, 9, 5)))
        assertTrue(specificOption.matchesDate(LocalDate.of(2026, 9, 20)))
        assertTrue(specificOption.matchesDate(LocalDate.of(2026, 10, 4)))

        assertFalse(specificOption.matchesDate(LocalDate.of(2026, 9, 4)))
        assertFalse(specificOption.matchesDate(LocalDate.of(2026, 10, 5)))

        val allTimeOption = CycleOption.AllTime
        assertTrue(allTimeOption.matchesDate(LocalDate.of(2020, 1, 1)))
        assertTrue(allTimeOption.matchesDate(LocalDate.of(2030, 12, 31)))
    }
}
