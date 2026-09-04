package com.vinay.monthlylekka.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate

data class BackupData(
    val tables: List<Lekka> = emptyList(),
    val categories: List<Category> = emptyList(),
    val expenses: List<Expense> = emptyList()
)

object DataExportManager {

    class LocalDateAdapter {
        @ToJson
        fun toJson(date: LocalDate): String = date.toString()

        @FromJson
        fun fromJson(dateString: String): LocalDate = LocalDate.parse(dateString)
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(LocalDateAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val backupAdapter by lazy {
        moshi.adapter(BackupData::class.java)
    }

    fun generateCsv(expenses: List<ExpenseWithCategoryAndLekka>): String {
        val sb = StringBuilder()
        sb.append("Date,Category,Description,Amount (INR),Type,Table Name\n")
        for (item in expenses) {
            val dateStr = escapeCsv(item.expense.date.toString())
            val categoryStr = escapeCsv(item.category.name)
            val descStr = escapeCsv(item.expense.description)
            val amountStr = escapeCsv(item.expense.amount.toString())
            val typeStr = if (item.category.isIncome) "Income" else "Expense"
            val tableNameStr = escapeCsv(item.lekkaName)

            sb.append("$dateStr,$categoryStr,$descStr,$amountStr,$typeStr,$tableNameStr\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    fun generateJsonBackup(
        tables: List<Lekka>,
        categories: List<Category>,
        expenses: List<Expense>
    ): String {
        val data = BackupData(tables = tables, categories = categories, expenses = expenses)
        return backupAdapter.indent("  ").toJson(data)
    }

    fun parseJsonBackup(jsonString: String): BackupData? {
        return try {
            backupAdapter.fromJson(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}
