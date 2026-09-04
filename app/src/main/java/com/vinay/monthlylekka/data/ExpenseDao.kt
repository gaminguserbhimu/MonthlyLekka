package com.vinay.monthlylekka.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("""
        SELECT e.*, l.name as lekkaName 
        FROM expenses e 
        JOIN lekkas l ON e.lekkaId = l.id 
        WHERE e.lekkaId = :lekkaId 
        ORDER BY e.date DESC
    """)
    fun getExpensesWithCategoryAndLekka(lekkaId: Long): Flow<List<ExpenseWithCategoryAndLekka>>

    @Transaction
    @Query("""
        SELECT e.*, l.name as lekkaName 
        FROM expenses e 
        JOIN lekkas l ON e.lekkaId = l.id 
        WHERE l.isMotherTable = 0 
        ORDER BY e.date DESC
    """)
    fun getAllExpensesWithCategoryAndLekka(): Flow<List<ExpenseWithCategoryAndLekka>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE lekkaId = :lekkaId ORDER BY date DESC")
    fun getExpensesWithCategory(lekkaId: Long): Flow<List<ExpenseWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Delete
    suspend fun deleteExpenses(expenses: List<Expense>)

    @Query("DELETE FROM expenses WHERE id IN (:ids)")
    suspend fun deleteExpensesByIds(ids: List<Long>)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("""
        SELECT 
            substr(e.date, 1, 7) as month,
            COALESCE(SUM(CASE WHEN c.isIncome = 1 THEN e.amount ELSE 0 END), 0.0) as totalIncome,
            COALESCE(SUM(CASE WHEN c.isIncome = 0 THEN e.amount ELSE 0 END), 0.0) as totalExpense
        FROM expenses e
        JOIN categories c ON e.categoryId = c.id
        WHERE e.lekkaId = :lekkaId
        GROUP BY month
        ORDER BY month DESC
    """)
    fun getMonthlySummaries(lekkaId: Long): Flow<List<MonthlySummary>>

    @Query("""
        SELECT 
            substr(e.date, 1, 7) as month,
            COALESCE(SUM(CASE WHEN c.isIncome = 1 THEN e.amount ELSE 0 END), 0.0) as totalIncome,
            COALESCE(SUM(CASE WHEN c.isIncome = 0 THEN e.amount ELSE 0 END), 0.0) as totalExpense
        FROM expenses e
        JOIN categories c ON e.categoryId = c.id
        JOIN lekkas l ON e.lekkaId = l.id
        WHERE l.isMotherTable = 0
        GROUP BY month
        ORDER BY month DESC
    """)
    fun getAllMonthlySummaries(): Flow<List<MonthlySummary>>

    @Query("""
        SELECT 
            :lekkaId as lekkaId,
            COALESCE(SUM(CASE WHEN c.isIncome = 1 THEN e.amount ELSE 0 END), 0.0) as totalIncome,
            COALESCE(SUM(CASE WHEN c.isIncome = 0 THEN e.amount ELSE 0 END), 0.0) as totalExpense
        FROM expenses e
        JOIN categories c ON e.categoryId = c.id
        WHERE e.lekkaId = :lekkaId
    """)
    fun getLekkaSummary(lekkaId: Long): Flow<LekkaSummary?>

    @Query("""
        SELECT 
            0 as lekkaId,
            COALESCE(SUM(CASE WHEN c.isIncome = 1 THEN e.amount ELSE 0 END), 0.0) as totalIncome,
            COALESCE(SUM(CASE WHEN c.isIncome = 0 THEN e.amount ELSE 0 END), 0.0) as totalExpense
        FROM expenses e
        JOIN categories c ON e.categoryId = c.id
        JOIN lekkas l ON e.lekkaId = l.id
        WHERE l.isMotherTable = 0
    """)
    fun getMotherTableSummary(): Flow<LekkaSummary?>
}
