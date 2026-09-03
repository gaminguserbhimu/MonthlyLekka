package com.vinay.monthlylekka.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val lekkaDao: LekkaDao
) {
    val allLekkas: Flow<List<Lekka>> = lekkaDao.getAllLekkas()

    fun getCategoriesByLekka(lekkaId: Long): Flow<List<Category>> = categoryDao.getAllCategories(lekkaId)
    
    fun getExpensesByLekka(lekkaId: Long): Flow<List<ExpenseWithCategory>> = expenseDao.getExpensesWithCategory(lekkaId)

    fun getExpensesWithCategoryAndLekka(lekkaId: Long): Flow<List<ExpenseWithCategoryAndLekka>> = expenseDao.getExpensesWithCategoryAndLekka(lekkaId)

    fun getAllExpensesWithCategoryAndLekka(): Flow<List<ExpenseWithCategoryAndLekka>> = expenseDao.getAllExpensesWithCategoryAndLekka()
    
    fun getMonthlySummariesByLekka(lekkaId: Long): Flow<List<MonthlySummary>> = expenseDao.getMonthlySummaries(lekkaId)

    fun getAllMonthlySummaries(): Flow<List<MonthlySummary>> = expenseDao.getAllMonthlySummaries()

    fun getLekkaSummary(lekkaId: Long): Flow<LekkaSummary?> = expenseDao.getLekkaSummary(lekkaId)

    fun getMotherTableSummary(): Flow<LekkaSummary?> = expenseDao.getMotherTableSummary()

    suspend fun insertLekka(lekka: Lekka): Long {
        return lekkaDao.insertLekka(lekka)
    }

    suspend fun updateLekka(lekka: Lekka) {
        lekkaDao.updateLekka(lekka)
    }

    suspend fun deleteLekka(lekka: Lekka) {
        lekkaDao.deleteLekka(lekka)
    }

    suspend fun setDefaultLekka(lekkaId: Long) {
        lekkaDao.setDefaultLekka(lekkaId)
    }

    suspend fun getDefaultLekka(): Lekka? {
        return lekkaDao.getDefaultLekka()
    }

    suspend fun getLekkaCount(): Int {
        return lekkaDao.getLekkaCount()
    }

    suspend fun getLekkaById(id: Long): Lekka? {
        return lekkaDao.getLekkaById(id)
    }

    suspend fun getMotherTable(): Lekka? {
        return lekkaDao.getMotherTable()
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun insertCategories(categories: List<Category>) {
        categoryDao.insertCategories(categories)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenses(expenses: List<Expense>) {
        expenseDao.deleteExpenses(expenses)
    }

    suspend fun deleteExpensesByIds(ids: List<Long>) {
        expenseDao.deleteExpensesByIds(ids)
    }
}
