package com.vinay.monthlylekka

import com.vinay.monthlylekka.data.AppRepository
import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.CategoryDao
import com.vinay.monthlylekka.data.CategorySpec
import com.vinay.monthlylekka.data.Expense
import com.vinay.monthlylekka.data.ExpenseDao
import com.vinay.monthlylekka.data.ExpenseWithCategory
import com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka
import com.vinay.monthlylekka.data.Lekka
import com.vinay.monthlylekka.data.LekkaDao
import com.vinay.monthlylekka.data.LekkaSummary
import com.vinay.monthlylekka.data.MonthlySummary
import com.vinay.monthlylekka.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeLekkaDao : LekkaDao {
        private val lekkas = MutableStateFlow<List<Lekka>>(emptyList())
        private var nextId = 1L

        override fun getAllLekkas(): Flow<List<Lekka>> = lekkas

        override suspend fun insertLekka(lekka: Lekka): Long {
            val id = if (lekka.id == 0L) nextId++ else lekka.id
            val newLekka = lekka.copy(id = id)
            lekkas.value = lekkas.value.filter { it.id != id } + newLekka
            return id
        }

        override suspend fun updateLekka(lekka: Lekka) {
            lekkas.value = lekkas.value.map { if (it.id == lekka.id) lekka else it }
        }

        override suspend fun deleteLekka(lekka: Lekka) {
            lekkas.value = lekkas.value.filter { it.id != lekka.id }
        }

        override suspend fun getLekkaById(id: Long): Lekka? = lekkas.value.find { it.id == id }

        override suspend fun getLekkaCount(): Int = lekkas.value.size

        override suspend fun setDefaultLekka(lekkaId: Long) {
            lekkas.value = lekkas.value.map { it.copy(isDefault = it.id == lekkaId) }
        }

        override suspend fun getDefaultLekka(): Lekka? = lekkas.value.find { it.isDefault }

        override suspend fun getMotherTable(): Lekka? = lekkas.value.find { it.isMotherTable }

        override fun getChildLekkas(): Flow<List<Lekka>> = lekkas.map { list -> list.filter { !it.isMotherTable } }

        override suspend fun deleteAllLekkas() {
            lekkas.value = emptyList()
        }
    }

    private class FakeCategoryDao : CategoryDao {
        val categories = MutableStateFlow<List<Category>>(emptyList())
        private var nextId = 1L

        override fun getAllCategories(lekkaId: Long): Flow<List<Category>> {
            return categories.map { list ->
                list.filter { it.lekkaId == lekkaId }
                    .sortedWith(compareByDescending<Category> { it.isIncome }.thenBy { it.name })
            }
        }

        override suspend fun insertCategory(category: Category) {
            val id = if (category.id == 0L) nextId++ else category.id
            categories.value = categories.value + category.copy(id = id)
        }

        override suspend fun insertCategories(categories: List<Category>) {
            val newCategories = categories.map { category ->
                val id = if (category.id == 0L) nextId++ else category.id
                category.copy(id = id)
            }
            this.categories.value = this.categories.value + newCategories
        }

        override suspend fun updateCategory(category: Category) {
            categories.value = this.categories.value.map { if (it.id == category.id) category else it }
        }

        override suspend fun deleteCategory(category: Category) {
            categories.value = this.categories.value.filter { it.id != category.id }
        }

        override suspend fun getCategoryById(id: Long): Category? = categories.value.find { it.id == id }

        override suspend fun deleteAllCategories() {
            categories.value = emptyList()
        }
    }

    private class FakeExpenseDao : ExpenseDao {
        val expenses = MutableStateFlow<List<ExpenseWithCategoryAndLekka>>(emptyList())

        override fun getExpensesWithCategoryAndLekka(lekkaId: Long): Flow<List<ExpenseWithCategoryAndLekka>> {
            return expenses.map { list -> list.filter { it.expense.lekkaId == lekkaId } }
        }

        override fun getAllExpensesWithCategoryAndLekka(): Flow<List<ExpenseWithCategoryAndLekka>> {
            return expenses
        }

        override fun getExpensesWithCategory(lekkaId: Long): Flow<List<ExpenseWithCategory>> {
            return expenses.map { list -> list.filter { it.expense.lekkaId == lekkaId }.map { it.toExpenseWithCategory() } }
        }

        override fun getMonthlySummaries(lekkaId: Long): Flow<List<MonthlySummary>> = MutableStateFlow(emptyList())

        override fun getAllMonthlySummaries(): Flow<List<MonthlySummary>> = MutableStateFlow(emptyList())

        override fun getLekkaSummary(lekkaId: Long): Flow<LekkaSummary?> = MutableStateFlow(LekkaSummary(lekkaId, 0.0, 0.0))

        override fun getMotherTableSummary(): Flow<LekkaSummary?> = MutableStateFlow(LekkaSummary(0, 5000.0, 2000.0))

        override suspend fun insertExpense(expense: Expense) {
            val id = if (expense.id == 0L) (expenses.value.size + 1).toLong() else expense.id
            val newExpense = expense.copy(id = id)
            val cat = Category(id = expense.categoryId, lekkaId = expense.lekkaId, name = "Food", colorHex = "#FF0000", isIncome = false)
            val item = ExpenseWithCategoryAndLekka(newExpense, cat, "Lekka $id")
            expenses.value = expenses.value + item
        }
        override suspend fun updateExpense(expense: Expense) {
            val cat = Category(id = expense.categoryId, lekkaId = expense.lekkaId, name = "Food", colorHex = "#FF0000", isIncome = false)
            val item = ExpenseWithCategoryAndLekka(expense, cat, "Lekka ${expense.id}")
            expenses.value = expenses.value.map { if (it.expense.id == expense.id) item else it }
        }
        override suspend fun deleteExpense(expense: Expense) {
            expenses.value = expenses.value.filter { it.expense.id != expense.id }
        }
        override suspend fun deleteExpenses(expenses: List<Expense>) {
            val deleteIds = expenses.map { it.id }.toSet()
            this.expenses.value = this.expenses.value.filter { it.expense.id !in deleteIds }
        }
        override suspend fun deleteExpensesByIds(ids: List<Long>) {
            val deleteIds = ids.toSet()
            this.expenses.value = this.expenses.value.filter { it.expense.id !in deleteIds }
        }
        override suspend fun deleteAllExpenses() {
            expenses.value = emptyList()
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_createsMasterLekkaAndChildTable() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        advanceUntilIdle()

        val lekkas = fakeLekkaDao.getAllLekkas().first()
        assertEquals(2, lekkas.size)
        
        val masterLekka = lekkas.find { it.isMotherTable }
        assertNotNull(masterLekka)
        assertEquals("Master Expense Sheet", masterLekka!!.name)
        assertFalse(masterLekka.isDefault)

        val childLekka = lekkas.find { !it.isMotherTable }
        assertNotNull(childLekka)
        assertEquals("Monthly Expenses", childLekka!!.name)
        assertTrue(childLekka.isDefault)
        assertEquals(masterLekka.id, viewModel.selectedLekkaId.value)
        assertTrue(viewModel.isMotherTableSelected.value)
    }

    @Test
    fun clearDatabase_clearsAllDataAndReseedsCleanState() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        advanceUntilIdle()

        viewModel.addExpense("Lunch", 250.0, 1L, LocalDate.now())
        advanceUntilIdle()
        assertEquals(1, fakeExpenseDao.expenses.value.size)

        viewModel.clearDatabase()
        advanceUntilIdle()

        assertEquals(0, fakeExpenseDao.expenses.value.size)

        val lekkas = fakeLekkaDao.getAllLekkas().first()
        assertEquals(2, lekkas.size)

        val childLekka = lekkas.find { !it.isMotherTable }
        assertNotNull(childLekka)
        assertEquals("Monthly Expenses", childLekka!!.name)
        assertTrue(childLekka.isDefault)

        val categories = fakeCategoryDao.getAllCategories(childLekka.id).first()
        assertEquals(8, categories.size)
    }

    @Test
    fun setDefaultLekka_updatesDefaultStatusCorrectly() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        advanceUntilIdle()

        viewModel.addLekka("Goa Trip")
        advanceUntilIdle()

        val lekkas = fakeLekkaDao.getAllLekkas().first()
        val goaTrip = lekkas.find { it.name == "Goa Trip" }!!

        assertFalse(goaTrip.isDefault)

        viewModel.setDefaultLekka(goaTrip.id)
        advanceUntilIdle()

        val updatedLekkas = fakeLekkaDao.getAllLekkas().first()
        val updatedGoaTrip = updatedLekkas.find { it.id == goaTrip.id }!!

        assertTrue(updatedGoaTrip.isDefault)
    }

    @Test
    fun addLekka_seedsDefaultCategories() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        advanceUntilIdle()

        viewModel.addLekka("New Trip Lekka", "2026-09-01", "2026-09-30")
        advanceUntilIdle()

        val lekkas = fakeLekkaDao.getAllLekkas().first()
        val newLekka = lekkas.find { it.name == "New Trip Lekka" }
        assertTrue("New Lekka should be created", newLekka != null)

        val categories = fakeCategoryDao.getAllCategories(newLekka!!.id).first()
        assertEquals(8, categories.size)

        val categoryNames = categories.map { it.name }
        assertTrue(categoryNames.contains("Kirani"))
        assertTrue(categoryNames.contains("Kaipalle"))
        assertTrue(categoryNames.contains("Food"))
        assertTrue(categoryNames.contains("Bills"))
        assertTrue(categoryNames.contains("Others"))
        assertTrue(categoryNames.contains("Travel"))
        assertTrue(categoryNames.contains("Hospital"))
        assertTrue(categoryNames.contains("Income"))
    }

    @Test
    fun addLekka_withCustomCategories_savesOnlySelectedAndCustomCategories() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        advanceUntilIdle()

        val customSpecs = listOf(
            CategorySpec("Food", "#E53935", isIncome = false),
            CategorySpec("Income", "#2E7D32", isIncome = true),
            CategorySpec("Fuel", "#00ACC1", isIncome = false, isDefault = false)
        )

        viewModel.addLekka("Custom Category Sheet", categories = customSpecs)
        advanceUntilIdle()

        val lekkas = fakeLekkaDao.getAllLekkas().first()
        val customSheet = lekkas.find { it.name == "Custom Category Sheet" }
        assertNotNull(customSheet)

        val categories = fakeCategoryDao.getAllCategories(customSheet!!.id).first()
        assertEquals(3, categories.size)

        val categoryNames = categories.map { it.name }
        assertTrue(categoryNames.contains("Food"))
        assertTrue(categoryNames.contains("Income"))
        assertTrue(categoryNames.contains("Fuel"))

        val fuelCategory = categories.find { it.name == "Fuel" }
        assertNotNull(fuelCategory)
        assertFalse(fuelCategory!!.isIncome) // Custom category strictly isIncome = false
    }

    @Test
    fun mostRecentSheet_computesSheetWithLatestTransactionCorrectly() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        val collectorJob = launch { viewModel.mostRecentSheet.collect {} }
        advanceUntilIdle()

        viewModel.addLekka("Sheet A")
        viewModel.addLekka("Sheet B")
        advanceUntilIdle()

        val lekkas = fakeLekkaDao.getAllLekkas().first()
        val sheetA = lekkas.find { it.name == "Sheet A" }!!
        val sheetB = lekkas.find { it.name == "Sheet B" }!!

        val cat = Category(id = 1, lekkaId = sheetA.id, name = "Food", colorHex = "#FF0000", isIncome = false)

        val expenseOld = Expense(id = 1, lekkaId = sheetA.id, description = "Old Expense", amount = 100.0, categoryId = 1, date = LocalDate.of(2026, 9, 1))
        val expenseNew = Expense(id = 2, lekkaId = sheetB.id, description = "New Expense", amount = 200.0, categoryId = 1, date = LocalDate.of(2026, 9, 5))

        fakeExpenseDao.expenses.value = listOf(
            ExpenseWithCategoryAndLekka(expenseOld, cat, "Sheet A"),
            ExpenseWithCategoryAndLekka(expenseNew, cat, "Sheet B")
        )

        advanceUntilIdle()

        val mostRecent = viewModel.mostRecentSheet.value
        assertNotNull(mostRecent)
        assertEquals(sheetB.id, mostRecent!!.id)
        assertEquals("Sheet B", mostRecent.name)
        collectorJob.cancel()
    }

    @Test
    fun deleteExpenses_batchDeletesCorrectly() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        advanceUntilIdle()

        val cat = Category(id = 1, lekkaId = 1, name = "Food", colorHex = "#FF0000", isIncome = false)
        val exp1 = Expense(id = 10, lekkaId = 1, description = "A", amount = 100.0, categoryId = 1, date = LocalDate.now())
        val exp2 = Expense(id = 20, lekkaId = 1, description = "B", amount = 200.0, categoryId = 1, date = LocalDate.now())
        val exp3 = Expense(id = 30, lekkaId = 1, description = "C", amount = 300.0, categoryId = 1, date = LocalDate.now())

        fakeExpenseDao.expenses.value = listOf(
            ExpenseWithCategoryAndLekka(exp1, cat, "Monthly Expenses"),
            ExpenseWithCategoryAndLekka(exp2, cat, "Monthly Expenses"),
            ExpenseWithCategoryAndLekka(exp3, cat, "Monthly Expenses")
        )

        viewModel.deleteExpenses(listOf(exp1, exp2))
        advanceUntilIdle()

        val remaining = fakeExpenseDao.expenses.value
        assertEquals(1, remaining.size)
        assertEquals(30L, remaining.first().expense.id)
    }

    @Test
    fun deleteExpensesByIds_batchDeletesByIdsCorrectly() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        advanceUntilIdle()

        val cat = Category(id = 1, lekkaId = 1, name = "Food", colorHex = "#FF0000", isIncome = false)
        val exp1 = Expense(id = 10, lekkaId = 1, description = "A", amount = 100.0, categoryId = 1, date = LocalDate.now())
        val exp2 = Expense(id = 20, lekkaId = 1, description = "B", amount = 200.0, categoryId = 1, date = LocalDate.now())

        fakeExpenseDao.expenses.value = listOf(
            ExpenseWithCategoryAndLekka(exp1, cat, "Monthly Expenses"),
            ExpenseWithCategoryAndLekka(exp2, cat, "Monthly Expenses")
        )

        viewModel.deleteExpensesByIds(listOf(10L))
        advanceUntilIdle()

        val remaining = fakeExpenseDao.expenses.value
        assertEquals(1, remaining.size)
        assertEquals(20L, remaining.first().expense.id)
    }

    @Test
    fun childLekkas_containsOnlyNonMotherTables() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        val collectorJob = launch { viewModel.childLekkas.collect {} }
        advanceUntilIdle()

        viewModel.addLekka("Goa Trip")
        advanceUntilIdle()

        val childLekkas = viewModel.childLekkas.value
        assertEquals(2, childLekkas.size)
        assertTrue(childLekkas.none { it.isMotherTable })
        assertTrue(childLekkas.any { it.name == "Monthly Expenses" })
        assertTrue(childLekkas.any { it.name == "Goa Trip" })

        collectorJob.cancel()
    }

    @Test
    fun addExpense_withTargetLekkaId_assignsToTargetLekka() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        advanceUntilIdle()

        val goaTripId = fakeLekkaDao.insertLekka(Lekka(name = "Goa Trip", isMotherTable = false))
        advanceUntilIdle()

        val expense = Expense(lekkaId = 1L, description = "Hotel", amount = 1500.0, categoryId = 1L, date = LocalDate.now())
        viewModel.addExpense(expense, targetLekkaId = goaTripId)
        advanceUntilIdle()

        val saved = fakeExpenseDao.expenses.value
        assertEquals(1, saved.size)
        assertEquals(goaTripId, saved.first().expense.lekkaId)
        assertEquals("Hotel", saved.first().expense.description)
    }

    @Test
    fun categories_incomeCategoryAlwaysReturnedFirst() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        val collectorJob = launch { viewModel.categories.collect {} }
        advanceUntilIdle()

        viewModel.selectLekka(2L)
        advanceUntilIdle()

        val categories = viewModel.categories.value
        if (categories.isNotEmpty()) {
            assertEquals("Income", categories.first().name)
            assertTrue(categories.first().isIncome)
        }
        collectorJob.cancel()
    }

    @Test
    fun deleteCategory_preventsIncomeCategoryDeletion() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        advanceUntilIdle()

        val incomeCategory = Category(id = 1, lekkaId = 2, name = "Income", colorHex = "#2E7D32", isIncome = true)
        val foodCategory = Category(id = 2, lekkaId = 2, name = "Food", colorHex = "#E53935", isIncome = false)
        fakeCategoryDao.categories.value = listOf(incomeCategory, foodCategory)

        viewModel.deleteCategory(incomeCategory)
        advanceUntilIdle()

        assertTrue("Income category must not be deleted", fakeCategoryDao.categories.value.contains(incomeCategory))

        viewModel.deleteCategory(foodCategory)
        advanceUntilIdle()

        assertFalse("Food category should be deleted", fakeCategoryDao.categories.value.contains(foodCategory))
    }

    @Test
    fun categories_updatesWhenSelectedLekkaChanges() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        val collectorJob = launch { viewModel.categories.collect {} }
        advanceUntilIdle()

        val lekka2Id = fakeLekkaDao.insertLekka(Lekka(name = "Trip Lekka", isMotherTable = false))
        advanceUntilIdle()

        viewModel.selectLekka(lekka2Id)
        advanceUntilIdle()

        val categories = viewModel.categories.value
        assertEquals(8, categories.size)
        assertTrue(categories.all { it.lekkaId == lekka2Id })
        collectorJob.cancel()
    }

    @Test
    fun categories_autoSeedsDefaultCategoriesIfTableHasNoCategories() = runTest {
        val fakeLekkaDao = FakeLekkaDao()
        val fakeCategoryDao = FakeCategoryDao()
        val fakeExpenseDao = FakeExpenseDao()
        val repository = AppRepository(fakeCategoryDao, fakeExpenseDao, fakeLekkaDao, ioDispatcher = testDispatcher)
        val viewModel = ExpenseViewModel(repository, ioDispatcher = testDispatcher)

        val collectorJob = launch { viewModel.categories.collect {} }
        advanceUntilIdle()

        val emptyLekkaId = fakeLekkaDao.insertLekka(Lekka(name = "Empty Lekka", isMotherTable = false))
        advanceUntilIdle()

        viewModel.selectLekka(emptyLekkaId)
        advanceUntilIdle()

        val categoriesInDb = fakeCategoryDao.getAllCategories(emptyLekkaId).first()
        assertEquals(8, categoriesInDb.size)
        assertTrue(categoriesInDb.any { it.name == "Kirani" })
        assertTrue(categoriesInDb.any { it.name == "Food" })
        collectorJob.cancel()
    }
}
