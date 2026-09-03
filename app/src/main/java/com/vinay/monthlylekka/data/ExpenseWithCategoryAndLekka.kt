package com.vinay.monthlylekka.data

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithCategoryAndLekka(
    @Embedded val expense: Expense,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category,
    val lekkaName: String = ""
) {
    fun toExpenseWithCategory(): ExpenseWithCategory = ExpenseWithCategory(expense, category)
}
