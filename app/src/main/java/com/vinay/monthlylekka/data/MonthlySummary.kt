package com.vinay.monthlylekka.data

data class MonthlySummary(
    val month: String,
    val totalIncome: Double,
    val totalExpense: Double
) {
    val netBalance: Double get() = totalIncome - totalExpense
}
