package com.vinay.monthlylekka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vinay.monthlylekka.data.AppRepository
import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.CategorySpec
import com.vinay.monthlylekka.data.DEFAULT_CATEGORY_SPECS
import com.vinay.monthlylekka.data.Expense
import com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka
import com.vinay.monthlylekka.data.Lekka
import com.vinay.monthlylekka.data.LekkaSummary
import com.vinay.monthlylekka.data.LekkaWithSummary
import com.vinay.monthlylekka.data.MonthlySummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Collections

class ExpenseViewModel(
    private val repository: AppRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _selectedLekkaId = MutableStateFlow<Long?>(null)
    val selectedLekkaId: StateFlow<Long?> = _selectedLekkaId.asStateFlow()

    val allLekkas: StateFlow<List<Lekka>> = repository.allLekkas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val childLekkas: StateFlow<List<Lekka>> = allLekkas
        .map { lekkas -> lekkas.filter { !it.isMotherTable } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isMotherTableSelected: StateFlow<Boolean> = combine(_selectedLekkaId, allLekkas) { id, lekkas ->
        val selected = lekkas.find { it.id == id }
        selected?.isMotherTable == true
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val motherTableSummary: StateFlow<LekkaSummary?> = repository.getMotherTableSummary()
        .map { summary -> summary ?: LekkaSummary(0, 0.0, 0.0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LekkaSummary(0, 0.0, 0.0)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val mostRecentSheet: StateFlow<Lekka?> = combine(allLekkas, repository.getAllExpensesWithCategoryAndLekka()) { lekkas, allExpenses ->
        val childLekkas = lekkas.filter { !it.isMotherTable }
        if (childLekkas.isEmpty()) return@combine null

        val latestExpense = allExpenses.maxWithOrNull(
            compareBy<ExpenseWithCategoryAndLekka> { it.expense.date }.thenBy { it.expense.id }
        )

        if (latestExpense != null) {
            childLekkas.find { it.id == latestExpense.expense.lekkaId }
                ?: childLekkas.find { it.isDefault }
                ?: childLekkas.firstOrNull()
        } else {
            childLekkas.find { it.isDefault } ?: childLekkas.firstOrNull()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allLekkasWithSummary: StateFlow<List<LekkaWithSummary>> = allLekkas
        .flatMapLatest { lekkas ->
            if (lekkas.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(lekkas.map { lekka ->
                val summaryFlow = if (lekka.isMotherTable) {
                    repository.getMotherTableSummary()
                } else {
                    repository.getLekkaSummary(lekka.id)
                }
                summaryFlow.map { summary ->
                    LekkaWithSummary(lekka, summary)
                }
            }) { it.toList() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<ExpenseWithCategoryAndLekka>> = combine(_selectedLekkaId, allLekkas) { id, lekkas ->
        Pair(id, lekkas)
    }.flatMapLatest { (id, lekkas) ->
        if (id == null) {
            flowOf(emptyList())
        } else {
            val selected = lekkas.find { it.id == id }
            if (selected?.isMotherTable == true) {
                repository.getAllExpensesWithCategoryAndLekka()
            } else {
                repository.getExpensesWithCategoryAndLekka(id)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val seedingLekkaIds = Collections.synchronizedSet(mutableSetOf<Long>())

    fun ensureDefaultCategoriesForLekka(lekkaId: Long) {
        if (lekkaId <= 0L) return
        if (seedingLekkaIds.add(lekkaId)) {
            viewModelScope.launch(ioDispatcher) {
                try {
                    val existing = repository.getCategoriesByLekka(lekkaId).firstOrNull()
                    if (existing.isNullOrEmpty()) {
                        seedDefaultCategories(lekkaId)
                    }
                } finally {
                    seedingLekkaIds.remove(lekkaId)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<Category>> = combine(_selectedLekkaId, allLekkas) { id, lekkas ->
        Pair(id, lekkas)
    }.flatMapLatest { (id, lekkas) ->
        val childLekkas = lekkas.filter { !it.isMotherTable }
        val targetId = if (id != null) {
            val selected = lekkas.find { it.id == id }
            if (selected?.isMotherTable == true) {
                childLekkas.find { it.isDefault }?.id ?: childLekkas.firstOrNull()?.id
            } else id
        } else {
            childLekkas.find { it.isDefault }?.id ?: childLekkas.firstOrNull()?.id
        }

        if (targetId != null) {
            repository.getCategoriesByLekka(targetId).map { cats ->
                if (cats.isEmpty()) {
                    ensureDefaultCategoriesForLekka(targetId)
                    DEFAULT_CATEGORY_SPECS.mapIndexed { index, spec ->
                        Category(
                            id = (index + 1).toLong(),
                            lekkaId = targetId,
                            name = spec.name,
                            colorHex = spec.colorHex,
                            isIncome = spec.isIncome
                        )
                    }
                } else {
                    cats
                }
            }
        } else {
            flowOf(
                DEFAULT_CATEGORY_SPECS.mapIndexed { index, spec ->
                    Category(
                        id = (index + 1).toLong(),
                        lekkaId = 0L,
                        name = spec.name,
                        colorHex = spec.colorHex,
                        isIncome = spec.isIncome
                    )
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DEFAULT_CATEGORY_SPECS.mapIndexed { index, spec ->
            Category(
                id = (index + 1).toLong(),
                lekkaId = 0L,
                name = spec.name,
                colorHex = spec.colorHex,
                isIncome = spec.isIncome
            )
        }
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlySummaries: StateFlow<List<MonthlySummary>> = combine(_selectedLekkaId, allLekkas) { id, lekkas ->
        Pair(id, lekkas)
    }.flatMapLatest { (id, lekkas) ->
        if (id == null) {
            flowOf(emptyList())
        } else {
            val selected = lekkas.find { it.id == id }
            if (selected?.isMotherTable == true) {
                repository.getAllMonthlySummaries()
            } else {
                repository.getMonthlySummariesByLekka(id)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch(ioDispatcher) {
            allLekkas.collect { lekkas ->
                if (lekkas.isEmpty()) {
                    repository.populateDatabase()
                    _selectedLekkaId.value = null
                } else if (_selectedLekkaId.value == null || lekkas.none { it.id == _selectedLekkaId.value }) {
                    val masterLekka = lekkas.find { it.isMotherTable }
                    val defaultLekka = masterLekka ?: lekkas.find { it.isDefault } ?: lekkas.firstOrNull()
                    defaultLekka?.let {
                        _selectedLekkaId.value = it.id
                    }
                }
            }
        }
    }

    fun clearDatabase() {
        viewModelScope.launch(ioDispatcher) {
            repository.clearDatabase()
        }
    }

    fun clearAllData() {
        clearDatabase()
    }

    fun selectLekka(id: Long) {
        _selectedLekkaId.value = id
    }

    fun setDefaultLekka(lekkaId: Long) {
        viewModelScope.launch(ioDispatcher) {
            repository.setDefaultLekka(lekkaId)
        }
    }

    fun addExpense(expense: Expense, targetLekkaId: Long? = null) {
        val lekkaId = targetLekkaId ?: expense.lekkaId.takeIf { it != 0L } ?: _selectedLekkaId.value ?: return
        viewModelScope.launch(ioDispatcher) {
            val existing = repository.getCategoriesByLekka(lekkaId).firstOrNull()
            val categories = if (existing.isNullOrEmpty()) {
                seedDefaultCategories(lekkaId)
                repository.getCategoriesByLekka(lekkaId).firstOrNull() ?: emptyList()
            } else {
                existing
            }
            val validCategoryId = categories.find { it.id == expense.categoryId }?.id
                ?: categories.firstOrNull()?.id
                ?: expense.categoryId

            val newExpense = expense.copy(lekkaId = lekkaId, categoryId = validCategoryId)
            repository.insertExpense(newExpense)
        }
    }

    fun addExpense(description: String, amount: Double, categoryId: Long, date: LocalDate, targetLekkaId: Long? = null) {
        val lekkaId = targetLekkaId ?: _selectedLekkaId.value ?: return
        addExpense(
            Expense(
                lekkaId = lekkaId,
                description = description,
                amount = amount,
                categoryId = categoryId,
                date = date
            ),
            targetLekkaId = lekkaId
        )
    }

    fun updateExpense(id: Long, description: String, amount: Double, categoryId: Long, date: LocalDate, targetLekkaId: Long? = null) {
        val lekkaId = targetLekkaId ?: _selectedLekkaId.value ?: return
        viewModelScope.launch(ioDispatcher) {
            val existing = repository.getCategoriesByLekka(lekkaId).firstOrNull()
            val categories = if (existing.isNullOrEmpty()) {
                seedDefaultCategories(lekkaId)
                repository.getCategoriesByLekka(lekkaId).firstOrNull() ?: emptyList()
            } else {
                existing
            }
            val validCategoryId = categories.find { it.id == categoryId }?.id
                ?: categories.firstOrNull()?.id
                ?: categoryId

            val updatedExpense = Expense(
                id = id,
                lekkaId = lekkaId,
                description = description,
                amount = amount,
                categoryId = validCategoryId,
                date = date
            )
            repository.updateExpense(updatedExpense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(ioDispatcher) {
            repository.deleteExpense(expense)
        }
    }

    fun deleteExpenses(expenses: List<Expense>) {
        viewModelScope.launch(ioDispatcher) {
            repository.deleteExpenses(expenses)
        }
    }

    fun deleteExpensesByIds(ids: List<Long>) {
        viewModelScope.launch(ioDispatcher) {
            repository.deleteExpensesByIds(ids)
        }
    }

    fun addCategory(name: String, colorHex: String, isIncome: Boolean = false) {
        val lekkaId = _selectedLekkaId.value ?: return
        viewModelScope.launch(ioDispatcher) {
            repository.insertCategory(Category(lekkaId = lekkaId, name = name, colorHex = colorHex, isIncome = isIncome))
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch(ioDispatcher) {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        if (category.isIncome || category.name.equals("Income", ignoreCase = true)) {
            return // System category cannot be deleted
        }
        viewModelScope.launch(ioDispatcher) {
            repository.deleteCategory(category)
        }
    }

    fun addLekka(
        name: String,
        startDate: String? = null,
        endDate: String? = null,
        isDefault: Boolean = false,
        categories: List<CategorySpec> = DEFAULT_CATEGORY_SPECS
    ) {
        viewModelScope.launch(ioDispatcher) {
            val lekkaId = repository.insertLekka(
                Lekka(
                    name = name,
                    startDate = startDate,
                    endDate = endDate,
                    isDefault = isDefault,
                    isMotherTable = false
                )
            )
            val categoriesToInsert = categories.map { spec ->
                Category(
                    lekkaId = lekkaId,
                    name = spec.name,
                    colorHex = spec.colorHex,
                    isIncome = spec.isIncome
                )
            }
            repository.insertCategories(categoriesToInsert)
            if (isDefault) {
                repository.setDefaultLekka(lekkaId)
                _selectedLekkaId.value = lekkaId
            }
        }
    }

    private suspend fun seedDefaultCategories(lekkaId: Long) {
        val defaultCategories = DEFAULT_CATEGORY_SPECS.map { spec ->
            Category(lekkaId = lekkaId, name = spec.name, colorHex = spec.colorHex, isIncome = spec.isIncome)
        }
        repository.insertCategories(defaultCategories)
    }

    fun updateLekka(
        lekka: Lekka,
        isDefault: Boolean = lekka.isDefault,
        categories: List<CategorySpec>? = null
    ) {
        viewModelScope.launch(ioDispatcher) {
            val updatedLekka = lekka.copy(isDefault = isDefault)
            repository.updateLekka(updatedLekka)
            if (isDefault) {
                repository.setDefaultLekka(lekka.id)
            }
            if (categories != null) {
                val existingCategories = repository.getCategoriesByLekka(lekka.id).firstOrNull() ?: emptyList()
                val existingNames = existingCategories.map { it.name }.toSet()
                val selectedNames = categories.map { it.name }.toSet()

                // Delete categories that are no longer selected
                existingCategories.filter { it.name !in selectedNames }.forEach {
                    repository.deleteCategory(it)
                }

                // Insert new categories that don't exist yet
                val newCategories = categories.filter { it.name !in existingNames }.map { spec ->
                    Category(
                        lekkaId = lekka.id,
                        name = spec.name,
                        colorHex = spec.colorHex,
                        isIncome = spec.isIncome
                    )
                }
                if (newCategories.isNotEmpty()) {
                    repository.insertCategories(newCategories)
                }
            }
        }
    }

    fun deleteLekka(lekka: Lekka) {
        if (lekka.isMotherTable) return // Mother Table cannot be deleted
        viewModelScope.launch(ioDispatcher) {
            repository.deleteLekka(lekka)
        }
    }
}

class ExpenseViewModelFactory(
    private val repository: AppRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository, ioDispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
