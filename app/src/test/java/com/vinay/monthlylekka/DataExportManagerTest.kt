package com.vinay.monthlylekka

import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.DataExportManager
import com.vinay.monthlylekka.data.Expense
import com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka
import com.vinay.monthlylekka.data.Lekka
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DataExportManagerTest {

    @Test
    fun generateCsv_headerAndRowsFormattedCorrectly() {
        val categoryExpense = Category(id = 1, lekkaId = 1, name = "Food", colorHex = "#FF0000", isIncome = false)
        val categoryIncome = Category(id = 2, lekkaId = 1, name = "Income", colorHex = "#00FF00", isIncome = true)

        val exp1 = Expense(id = 10, lekkaId = 1, description = "Lunch, Special", amount = 250.0, categoryId = 1, date = LocalDate.of(2026, 3, 31))
        val exp2 = Expense(id = 20, lekkaId = 1, description = "Salary \"Bonus\"", amount = 50000.0, categoryId = 2, date = LocalDate.of(2026, 3, 1))

        val items = listOf(
            ExpenseWithCategoryAndLekka(exp1, categoryExpense, "Home Expenses"),
            ExpenseWithCategoryAndLekka(exp2, categoryIncome, "Home Expenses")
        )

        val csv = DataExportManager.generateCsv(items)
        val lines = csv.trim().split("\n")

        assertEquals(3, lines.size)
        assertEquals("Date,Category,Description,Amount (INR),Type,Table Name", lines[0])

        // Verify CSV escaping for commas and quotes
        assertTrue(lines[1].contains("\"Lunch, Special\""))
        assertTrue(lines[1].contains("Expense"))

        assertTrue(lines[2].contains("\"Salary \"\"Bonus\"\"\""))
        assertTrue(lines[2].contains("Income"))
    }

    @Test
    fun jsonBackup_exportAndParseRoundTrip() {
        val tables = listOf(
            Lekka(id = 1, name = "Master Expense Table", isMotherTable = true, isDefault = false),
            Lekka(id = 2, name = "Monthly Expenses", isMotherTable = false, isDefault = true)
        )
        val categories = listOf(
            Category(id = 10, lekkaId = 2, name = "Income", colorHex = "#2E7D32", isIncome = true),
            Category(id = 11, lekkaId = 2, name = "Kirani", colorHex = "#FFB300", isIncome = false)
        )
        val expenses = listOf(
            Expense(id = 100, lekkaId = 2, description = "Groceries", amount = 1200.0, categoryId = 11, date = LocalDate.of(2026, 3, 15))
        )

        val jsonString = DataExportManager.generateJsonBackup(tables, categories, expenses)
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("Master Expense Table"))
        assertTrue(jsonString.contains("Groceries"))

        val parsedBackup = DataExportManager.parseJsonBackup(jsonString)
        assertNotNull(parsedBackup)
        assertEquals(2, parsedBackup!!.tables.size)
        assertEquals(2, parsedBackup.categories.size)
        assertEquals(1, parsedBackup.expenses.size)

        assertEquals("Monthly Expenses", parsedBackup.tables[1].name)
        assertEquals("Kirani", parsedBackup.categories[1].name)
        assertEquals("Groceries", parsedBackup.expenses[0].description)
        assertEquals(LocalDate.of(2026, 3, 15), parsedBackup.expenses[0].date)
    }

    @Test
    fun parseJsonBackup_returnsNullForInvalidJson() {
        val invalidJson = "{ invalid_json: "
        val result = DataExportManager.parseJsonBackup(invalidJson)
        assertNull(result)
    }
}
