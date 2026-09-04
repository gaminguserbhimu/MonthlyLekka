package com.vinay.monthlylekka.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val lekkaDao: LekkaDao,
    private val database: AppDatabase? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val allLekkas: Flow<List<Lekka>> = lekkaDao.getAllLekkas()

    fun getCategoriesByLekka(lekkaId: Long): Flow<List<Category>> = categoryDao.getCategoriesByLekka(lekkaId)

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategoriesList()
    
    fun getExpensesByLekka(lekkaId: Long): Flow<List<ExpenseWithCategory>> = expenseDao.getExpensesWithCategory(lekkaId)

    fun getExpensesWithCategoryAndLekka(lekkaId: Long): Flow<List<ExpenseWithCategoryAndLekka>> = expenseDao.getExpensesWithCategoryAndLekka(lekkaId)

    fun getAllExpensesWithCategoryAndLekka(): Flow<List<ExpenseWithCategoryAndLekka>> = expenseDao.getAllExpensesWithCategoryAndLekka()

    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpensesList()
    
    fun getMonthlySummariesByLekka(lekkaId: Long): Flow<List<MonthlySummary>> = expenseDao.getMonthlySummaries(lekkaId)

    fun getAllMonthlySummaries(): Flow<List<MonthlySummary>> = expenseDao.getAllMonthlySummaries()

    fun getLekkaSummary(lekkaId: Long): Flow<LekkaSummary?> = expenseDao.getLekkaSummary(lekkaId)

    fun getMotherTableSummary(): Flow<LekkaSummary?> = expenseDao.getMotherTableSummary()

    suspend fun populateDatabase() = withContext(Dispatchers.IO) {
        if (lekkaDao.getLekkaCount() == 0) {
            if (database != null) {
                database.populateDatabase()
            } else {
                lekkaDao.insertLekka(Lekka(name = "Master Expense Table", isMotherTable = true, isDefault = false))
                val defaultChildLekkaId = lekkaDao.insertLekka(Lekka(name = "Monthly Expenses", isMotherTable = false, isDefault = true))

                val categories = listOf(
                    Category(lekkaId = defaultChildLekkaId, name = "Income", colorHex = "#2E7D32", isIncome = true),
                    Category(lekkaId = defaultChildLekkaId, name = "Kirani", colorHex = "#FFB300", isIncome = false),
                    Category(lekkaId = defaultChildLekkaId, name = "Kaipalle", colorHex = "#43A047", isIncome = false),
                    Category(lekkaId = defaultChildLekkaId, name = "Food", colorHex = "#E53935", isIncome = false),
                    Category(lekkaId = defaultChildLekkaId, name = "Bills", colorHex = "#3949AB", isIncome = false),
                    Category(lekkaId = defaultChildLekkaId, name = "Others", colorHex = "#757575", isIncome = false),
                    Category(lekkaId = defaultChildLekkaId, name = "Travel", colorHex = "#1E88E5", isIncome = false),
                    Category(lekkaId = defaultChildLekkaId, name = "Hospital", colorHex = "#D81B60", isIncome = false)
                )
                categories.forEach { categoryDao.insertCategory(it) }
            }
        }
    }

    suspend fun clearDatabase() = withContext(ioDispatcher) {
        clearAllData()
    }

    suspend fun clearAllData() = withContext(ioDispatcher) {
        if (database != null) {
            database.clearDatabase()
        } else {
            expenseDao.deleteAllExpenses()
            categoryDao.deleteAllCategories()
            lekkaDao.deleteAllLekkas()

            val masterLekkaId = lekkaDao.insertLekka(Lekka(name = "Master Expense Table", isMotherTable = true, isDefault = false))
            val defaultChildLekkaId = lekkaDao.insertLekka(Lekka(name = "Monthly Expenses", isMotherTable = false, isDefault = true))

            val categories = listOf(
                Category(lekkaId = defaultChildLekkaId, name = "Income", colorHex = "#2E7D32", isIncome = true),
                Category(lekkaId = defaultChildLekkaId, name = "Kirani", colorHex = "#FFB300", isIncome = false),
                Category(lekkaId = defaultChildLekkaId, name = "Kaipalle", colorHex = "#43A047", isIncome = false),
                Category(lekkaId = defaultChildLekkaId, name = "Food", colorHex = "#E53935", isIncome = false),
                Category(lekkaId = defaultChildLekkaId, name = "Bills", colorHex = "#3949AB", isIncome = false),
                Category(lekkaId = defaultChildLekkaId, name = "Others", colorHex = "#757575", isIncome = false),
                Category(lekkaId = defaultChildLekkaId, name = "Travel", colorHex = "#1E88E5", isIncome = false),
                Category(lekkaId = defaultChildLekkaId, name = "Hospital", colorHex = "#D81B60", isIncome = false)
            )
            categories.forEach { categoryDao.insertCategory(it) }
        }
    }

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

    suspend fun updateMotherTableName() = withContext(ioDispatcher) {
        lekkaDao.updateMotherTableName()
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

    suspend fun restoreBackupData(backupData: BackupData) = withContext(ioDispatcher) {
        if (database != null) {
            database.clearAllTables()
        } else {
            expenseDao.deleteAllExpenses()
            categoryDao.deleteAllCategories()
            lekkaDao.deleteAllLekkas()
        }

        if (backupData.tables.isNotEmpty()) {
            lekkaDao.insertLekkas(backupData.tables)
        }

        if (lekkaDao.getMotherTable() == null) {
            lekkaDao.insertLekka(Lekka(name = "Master Expense Table", isMotherTable = true, isDefault = false))
        }

        if (backupData.categories.isNotEmpty()) {
            categoryDao.insertCategories(backupData.categories)
        }

        val insertedLekkaIds = backupData.tables.map { it.id }.toSet()
        val insertedCategoryIds = backupData.categories.map { it.id }.toSet()

        val validExpenses = backupData.expenses.filter {
            (insertedLekkaIds.isEmpty() || it.lekkaId in insertedLekkaIds) &&
            (insertedCategoryIds.isEmpty() || it.categoryId in insertedCategoryIds)
        }

        if (validExpenses.isNotEmpty()) {
            expenseDao.insertExpenses(validExpenses)
        }
    }
}
