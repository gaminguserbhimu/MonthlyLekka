package com.vinay.monthlylekka.data

data class LekkaSummary(
    val lekkaId: Long,
    val totalIncome: Double,
    val totalExpense: Double
) {
    val balance: Double get() = totalIncome - totalExpense
}
