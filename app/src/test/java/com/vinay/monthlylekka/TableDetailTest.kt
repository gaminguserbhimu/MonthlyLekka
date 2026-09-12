package com.vinay.monthlylekka

import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.Expense
import com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka
import com.vinay.monthlylekka.data.MonthlySummary
import com.vinay.monthlylekka.ui.Route
import com.vinay.monthlylekka.ui.YearlySummary
import com.vinay.monthlylekka.ui.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import kotlin.math.abs

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
        val cat1 = Category(id = 1, lekkaId = 1, name = "Groceries", colorHex = "#FFB300", isIncome = false)
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
            expense = Expense(id = 4, lekkaId = 1, description = "Groceries Old", amount = 3000.0, categoryId = 1, date = LocalDate.of(2025, 12, 20)),
            category = cat1,
            lekkaName = "Monthly Lekka"
        )

        val expenses = listOf(exp1, exp2, exp3, exp4)

        // Monthly Category Breakdown for Sep 2026
        val sep2026Expenses = expenses.filter { java.time.YearMonth.from(it.expense.date) == java.time.YearMonth.of(2026, 9) }
        val sep2026CategoryMap = sep2026Expenses.groupBy { it.category.name }
            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

        assertEquals(2, sep2026CategoryMap.size)
        assertEquals(1500.0, sep2026CategoryMap["Groceries"] ?: 0.0, 0.01)
        assertEquals(2000.0, sep2026CategoryMap["Food"] ?: 0.0, 0.01)
        assertEquals(3500.0, sep2026CategoryMap.values.sum(), 0.01)

        // Yearly Category Breakdown for 2026
        val year2026Expenses = expenses.filter { it.expense.date.year == 2026 }
        val year2026CategoryMap = year2026Expenses.groupBy { it.category.name }
            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

        assertEquals(2, year2026CategoryMap.size)
        assertEquals(1500.0, year2026CategoryMap["Groceries"] ?: 0.0, 0.01)
        assertEquals(2000.0, year2026CategoryMap["Food"] ?: 0.0, 0.01)
        assertEquals(3500.0, year2026CategoryMap.values.sum(), 0.01)
    }

    @Test
    fun topSummaryFormula_formatsNetBalanceWithSignAndAbsoluteValue() {
        val positiveNetBalance = 153.0
        val isPositive1 = positiveNetBalance >= 0
        val absBalanceStr1 = abs(positiveNetBalance).toCurrencyString()
        val resultText1 = if (isPositive1) "+ $absBalanceStr1" else "- $absBalanceStr1"
        assertEquals("+ ₹ 153", resultText1)

        val negativeNetBalance = -153.0
        val isPositive2 = negativeNetBalance >= 0
        val absBalanceStr2 = abs(negativeNetBalance).toCurrencyString()
        val resultText2 = if (isPositive2) "+ $absBalanceStr2" else "- $absBalanceStr2"
        assertEquals("- ₹ 153", resultText2)
    }

    @Test
    fun filterTab_filtersTransactionsAndCalculatesTotalsCorrectly() {
        val incomeCat =
            Category(id = 1, lekkaId = 1, name = "Salary", colorHex = "#10B981", isIncome = true)
        val foodCat = Category(id = 2, lekkaId = 1, name = "Food", colorHex = "#E53935", isIncome = false)

        val item1 = ExpenseWithCategoryAndLekka(
            expense = Expense(id = 1, lekkaId = 1, description = "Salary", amount = 50000.0, categoryId = 1, date = LocalDate.of(2026, 9, 1)),
            category = incomeCat,
            lekkaName = "Monthly Lekka"
        )
        val item2 = ExpenseWithCategoryAndLekka(
            expense = Expense(id = 2, lekkaId = 1, description = "Groceries", amount = 3000.0, categoryId = 2, date = LocalDate.of(2026, 9, 10)),
            category = foodCat,
            lekkaName = "Monthly Lekka"
        )
        val item3 = ExpenseWithCategoryAndLekka(
            expense = Expense(id = 3, lekkaId = 1, description = "Restaurant", amount = 1500.0, categoryId = 2, date = LocalDate.of(2026, 9, 25)),
            category = foodCat,
            lekkaName = "Monthly Lekka"
        )
        val item4 = ExpenseWithCategoryAndLekka(
            expense = Expense(id = 4, lekkaId = 1, description = "Old Expense", amount = 2000.0, categoryId = 2, date = LocalDate.of(2026, 8, 15)),
            category = foodCat,
            lekkaName = "Monthly Lekka"
        )

        val expenses = listOf(item1, item2, item3, item4)

        val fromDate = LocalDate.of(2026, 9, 1)
        val toDate = LocalDate.of(2026, 9, 20)

        val filtered = expenses.filter {
            val d = it.expense.date
            !d.isBefore(fromDate) && !d.isAfter(toDate)
        }

        assertEquals(2, filtered.size)
        val totalIncome = filtered.filter { it.category.isIncome }.sumOf { it.expense.amount }
        val totalOutcome = filtered.filter { !it.category.isIncome }.sumOf { it.expense.amount }
        val netBalance = totalIncome - totalOutcome

        assertEquals(50000.0, totalIncome, 0.01)
        assertEquals(3000.0, totalOutcome, 0.01)
        assertEquals(47000.0, netBalance, 0.01)
    }

    @Test
    fun filterTab_filtersByDateAndCategoryCorrectly() {
        val incomeCat = Category(id = 1, lekkaId = 1, name = "Salary", colorHex = "#10B981", isIncome = true)
        val foodCat = Category(id = 2, lekkaId = 1, name = "Food", colorHex = "#E53935", isIncome = false)
        val travelCat = Category(id = 3, lekkaId = 1, name = "Travel", colorHex = "#3B82F6", isIncome = false)

        val item1 = ExpenseWithCategoryAndLekka(
            expense = Expense(id = 1, lekkaId = 1, description = "Salary", amount = 50000.0, categoryId = 1, date = LocalDate.of(2026, 9, 1)),
            category = incomeCat,
            lekkaName = "Monthly Lekka"
        )
        val item2 = ExpenseWithCategoryAndLekka(
            expense = Expense(id = 2, lekkaId = 1, description = "Groceries", amount = 3000.0, categoryId = 2, date = LocalDate.of(2026, 9, 10)),
            category = foodCat,
            lekkaName = "Monthly Lekka"
        )
        val item3 = ExpenseWithCategoryAndLekka(
            expense = Expense(id = 3, lekkaId = 1, description = "Bus Ticket", amount = 500.0, categoryId = 3, date = LocalDate.of(2026, 9, 15)),
            category = travelCat,
            lekkaName = "Monthly Lekka"
        )

        val expenses = listOf(item1, item2, item3)
        val fromDate = LocalDate.of(2026, 9, 1)
        val toDate = LocalDate.of(2026, 9, 20)

        // All Categories selected
        val selectedCategoryAll = "All Categories"
        val filteredAll = expenses.filter { item ->
            val d = item.expense.date
            val dateInRange = !d.isBefore(fromDate) && !d.isAfter(toDate)
            val categoryMatches = selectedCategoryAll == "All Categories" || item.category.name == selectedCategoryAll
            dateInRange && categoryMatches
        }
        assertEquals(3, filteredAll.size)

        // Specific Category "Food" selected
        val selectedCategoryFood = "Food"
        val filteredFood = expenses.filter { item ->
            val d = item.expense.date
            val dateInRange = !d.isBefore(fromDate) && !d.isAfter(toDate)
            val categoryMatches = selectedCategoryFood == "All Categories" || item.category.name == selectedCategoryFood
            dateInRange && categoryMatches
        }
        assertEquals(1, filteredFood.size)
        assertEquals("Groceries", filteredFood.first().expense.description)
        assertEquals("Food", filteredFood.first().category.name)
    }

    @Test
    fun expenseDescription_fallbackToCategoryNameWhenBlank() {
        val foodCat = Category(id = 2, lekkaId = 1, name = "Food", colorHex = "#E53935", isIncome = false)

        val expWithBlankDesc = Expense(id = 1, lekkaId = 1, description = "", amount = 250.0, categoryId = 2, date = LocalDate.now())
        val displayText1 = expWithBlankDesc.description.ifBlank { foodCat.name }
        assertEquals("Food", displayText1)

        val expWithSpaceDesc = Expense(id = 2, lekkaId = 1, description = "   ", amount = 150.0, categoryId = 2, date = LocalDate.now())
        val displayText2 = expWithSpaceDesc.description.ifBlank { foodCat.name }
        assertEquals("Food", displayText2)

        val expWithNormalDesc = Expense(id = 3, lekkaId = 1, description = "Snacks", amount = 100.0, categoryId = 2, date = LocalDate.now())
        val displayText3 = expWithNormalDesc.description.ifBlank { foodCat.name }
        assertEquals("Snacks", displayText3)
    }
}

