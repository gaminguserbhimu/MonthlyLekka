package com.vinay.monthlylekka

import com.vinay.monthlylekka.data.MonthlySummary
import com.vinay.monthlylekka.ui.Route
import com.vinay.monthlylekka.ui.YearlySummary
import org.junit.Assert.assertEquals
import org.junit.Test

class TableDetailTest {

    @Test
    fun yearlySummary_calculatesNetBalanceCorrectly() {
        val yearlySummary = YearlySummary(
            year = "2026",
            totalIncome = 100000.0,
            totalExpense = 45000.0
        )

        assertEquals("2026", yearlySummary.year)
        assertEquals(100000.0, yearlySummary.totalIncome, 0.01)
        assertEquals(45000.0, yearlySummary.totalExpense, 0.01)
        assertEquals(55000.0, yearlySummary.netBalance, 0.01)
    }

    @Test
    fun yearlySummary_aggregatesMonthlySummariesCorrectly() {
        val monthlySummaries = listOf(
            MonthlySummary("2026-08", 50000.0, 20000.0),
            MonthlySummary("2026-09", 60000.0, 25000.0),
            MonthlySummary("2025-12", 40000.0, 15000.0)
        )

        val aggregatedYearly = monthlySummaries
            .groupBy { it.month.take(4) }
            .map { (year, list) ->
                YearlySummary(
                    year = year,
                    totalIncome = list.sumOf { it.totalIncome },
                    totalExpense = list.sumOf { it.totalExpense }
                )
            }
            .sortedByDescending { it.year }

        assertEquals(2, aggregatedYearly.size)

        val year2026 = aggregatedYearly.find { it.year == "2026" }
        assertEquals(110000.0, year2026?.totalIncome ?: 0.0, 0.01)
        assertEquals(45000.0, year2026?.totalExpense ?: 0.0, 0.01)
        assertEquals(65000.0, year2026?.netBalance ?: 0.0, 0.01)

        val year2025 = aggregatedYearly.find { it.year == "2025" }
        assertEquals(40000.0, year2025?.totalIncome ?: 0.0, 0.01)
        assertEquals(15000.0, year2025?.totalExpense ?: 0.0, 0.01)
        assertEquals(25000.0, year2025?.netBalance ?: 0.0, 0.01)
    }

    @Test
    fun route_tableDetail_instantiatesWithLekkaId() {
        val route: Route = Route.TableDetail(lekkaId = 42L)
        assert(route is Route.TableDetail)
        assertEquals(42L, (route as Route.TableDetail).lekkaId)
    }

    @Test
    fun route_help_instantiatesCorrectly() {
        val route: Route = Route.Help
        assert(route is Route.Help)
        assertEquals(Route.Help, route)
    }

    @Test
    fun categoryBreakdown_groupsMonthlyAndYearlyExpensesCorrectly() {
        val cat1 = com.vinay.monthlylekka.data.Category(id = 1, lekkaId = 1, name = "Kirani", colorHex = "#FFB300", isIncome = false)
        val cat2 = com.vinay.monthlylekka.data.Category(id = 2, lekkaId = 1, name = "Food", colorHex = "#E53935", isIncome = false)

        val exp1 = com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka(
            expense = com.vinay.monthlylekka.data.Expense(id = 1, lekkaId = 1, description = "Rice", amount = 1000.0, categoryId = 1, date = java.time.LocalDate.of(2026, 9, 1)),
            category = cat1,
            lekkaName = "Monthly Lekka"
        )
        val exp2 = com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka(
            expense = com.vinay.monthlylekka.data.Expense(id = 2, lekkaId = 1, description = "Wheat", amount = 500.0, categoryId = 1, date = java.time.LocalDate.of(2026, 9, 5)),
            category = cat1,
            lekkaName = "Monthly Lekka"
        )
        val exp3 = com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka(
            expense = com.vinay.monthlylekka.data.Expense(id = 3, lekkaId = 1, description = "Restaurant", amount = 2000.0, categoryId = 2, date = java.time.LocalDate.of(2026, 9, 10)),
            category = cat2,
            lekkaName = "Monthly Lekka"
        )
        val exp4 = com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka(
            expense = com.vinay.monthlylekka.data.Expense(id = 4, lekkaId = 1, description = "Kirani Old", amount = 3000.0, categoryId = 1, date = java.time.LocalDate.of(2025, 12, 20)),
            category = cat1,
            lekkaName = "Monthly Lekka"
        )

        val expenses = listOf(exp1, exp2, exp3, exp4)

        // Monthly Category Breakdown for Sep 2026
        val sep2026Expenses = expenses.filter { java.time.YearMonth.from(it.expense.date) == java.time.YearMonth.of(2026, 9) }
        val sep2026CategoryMap = sep2026Expenses.groupBy { it.category.name }
            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

        assertEquals(2, sep2026CategoryMap.size)
        assertEquals(1500.0, sep2026CategoryMap["Kirani"] ?: 0.0, 0.01)
        assertEquals(2000.0, sep2026CategoryMap["Food"] ?: 0.0, 0.01)
        assertEquals(3500.0, sep2026CategoryMap.values.sum(), 0.01)

        // Yearly Category Breakdown for 2026
        val year2026Expenses = expenses.filter { it.expense.date.year == 2026 }
        val year2026CategoryMap = year2026Expenses.groupBy { it.category.name }
            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

        assertEquals(2, year2026CategoryMap.size)
        assertEquals(1500.0, year2026CategoryMap["Kirani"] ?: 0.0, 0.01)
        assertEquals(2000.0, year2026CategoryMap["Food"] ?: 0.0, 0.01)
        assertEquals(3500.0, year2026CategoryMap.values.sum(), 0.01)
    }
}
