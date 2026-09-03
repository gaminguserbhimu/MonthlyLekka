package com.vinay.monthlylekka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.vinay.monthlylekka.ui.AddExpenseScreen
import com.vinay.monthlylekka.ui.AnalyticsScreen
import com.vinay.monthlylekka.ui.CategoryManagementScreen
import com.vinay.monthlylekka.ui.Route
import com.vinay.monthlylekka.ui.TableDetailScreen
import com.vinay.monthlylekka.ui.TablesScreen
import com.vinay.monthlylekka.ui.WelcomeScreen
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme
import com.vinay.monthlylekka.ui.viewmodel.ExpenseViewModel
import com.vinay.monthlylekka.ui.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MonthlyLekkaTheme {
                val viewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModelFactory((application as MonthlyLekkaApplication).repository)
                )
                var backStack by rememberSaveable { mutableStateOf(listOf<Route>(Route.Welcome)) }
                val allLekkasWithSummary by viewModel.allLekkasWithSummary.collectAsState()
                val selectedLekkaId by viewModel.selectedLekkaId.collectAsState()
                val isMotherTableSelected by viewModel.isMotherTableSelected.collectAsState()
                val motherTableSummary by viewModel.motherTableSummary.collectAsState()
                val mostRecentSheet by viewModel.mostRecentSheet.collectAsState()
                
                val expenses by viewModel.expenses.collectAsState()
                val categories by viewModel.categories.collectAsState()
                val monthlySummaries by viewModel.monthlySummaries.collectAsState()
                
                val childLekkas = remember(allLekkasWithSummary) {
                    allLekkasWithSummary.filter { !it.lekka.isMotherTable }.map { it.lekka }
                }

                val navigator = rememberListDetailPaneScaffoldNavigator<Route>()
                
                // Synchronize backStack with navigator for adaptive layout
                LaunchedEffect(backStack.size) {
                    val currentRoute = backStack.last()
                    
                    // Update ViewModel's selectedLekkaId based on route
                    when (currentRoute) {
                        is Route.Dashboard -> viewModel.selectLekka(currentRoute.lekkaId)
                        is Route.TableDetail -> viewModel.selectLekka(currentRoute.lekkaId)
                        is Route.AddExpense -> viewModel.selectLekka(currentRoute.lekkaId)
                        is Route.CategoryManagement -> viewModel.selectLekka(currentRoute.lekkaId)
                        is Route.Analytics -> viewModel.selectLekka(currentRoute.lekkaId)
                        else -> {}
                    }

                    when (currentRoute) {
                        is Route.AddExpense -> navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, currentRoute)
                        is Route.CategoryManagement -> navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, currentRoute)
                        is Route.Analytics -> navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, currentRoute)
                        is Route.Dashboard -> navigator.navigateTo(ListDetailPaneScaffoldRole.List)
                        is Route.TableDetail -> navigator.navigateTo(ListDetailPaneScaffoldRole.List)
                        is Route.Tables -> navigator.navigateTo(ListDetailPaneScaffoldRole.List)
                        is Route.Welcome -> { /* Full screen */ }
                    }
                }

                if (navigator.scaffoldDirective.maxHorizontalPartitions > 1 && backStack.last() !is Route.Welcome) {
                    ListDetailPaneScaffold(
                        directive = navigator.scaffoldDirective,
                        value = navigator.scaffoldValue,
                        listPane = {
                            AnimatedPane {
                                val lastRoute = backStack.last()
                                if (lastRoute is Route.Tables) {
                                    TablesScreen(
                                        lekkas = allLekkasWithSummary,
                                        motherTableSummary = motherTableSummary,
                                        onBack = {
                                            backStack = backStack.filterIsInstance<Route.Welcome>()
                                        },
                                        onTableClick = { id ->
                                            viewModel.selectLekka(id)
                                            backStack = backStack + Route.TableDetail(id)
                                        },
                                        onSetDefaultLekka = viewModel::setDefaultLekka,
                                        onCreateLekka = { name, startDate, endDate, isDefault, categories ->
                                            viewModel.addLekka(name, startDate, endDate, isDefault, categories)
                                        },
                                        onUpdateLekka = { lekka, isDefault, categories ->
                                            viewModel.updateLekka(lekka, isDefault, categories)
                                        },
                                        onDeleteLekka = viewModel::deleteLekka
                                    )
                                } else {
                                    val currentLekkaWithSummary = allLekkasWithSummary.find { it.lekka.id == selectedLekkaId }
                                    val currentLekkaName = currentLekkaWithSummary?.lekka?.name ?: "Unknown"
                                    val isMother = currentLekkaWithSummary?.lekka?.isMotherTable == true

                                    TableDetailScreen(
                                        lekkaName = currentLekkaName,
                                        isMotherTable = isMother,
                                        childLekkas = childLekkas,
                                        expenses = expenses,
                                        monthlySummaries = monthlySummaries,
                                        categories = categories,
                                        onBack = {
                                            backStack = backStack.filterIsInstance<Route.Welcome>()
                                        },
                                        onAddExpenseClick = { targetId ->
                                            if (backStack.none { it is Route.AddExpense && it.expenseId == null }) {
                                                backStack = backStack + Route.AddExpense(targetId)
                                            }
                                        },
                                        onExpenseClick = { item ->
                                            backStack = backStack + Route.AddExpense(item.expense.lekkaId, item.expense.id)
                                        },
                                        onDeleteExpense = { viewModel.deleteExpense(it.expense) },
                                        onManageCategoriesClick = {
                                            selectedLekkaId?.let { id ->
                                                if (backStack.none { it is Route.CategoryManagement }) {
                                                    backStack = backStack + Route.CategoryManagement(id)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        detailPane = {
                            AnimatedPane {
                                val lastRoute = backStack.last()
                                when (lastRoute) {
                                    is Route.AddExpense -> {
                                        val expenseItem = expenses.find { it.expense.id == lastRoute.expenseId }
                                        AddExpenseScreen(
                                            categories = categories,
                                            expenseToEdit = expenseItem?.toExpenseWithCategory(),
                                            onSave = { id, desc, amount, categoryId, date ->
                                                if (id != null) {
                                                    viewModel.updateExpense(id, desc, amount, categoryId, date, targetLekkaId = lastRoute.lekkaId)
                                                } else {
                                                    viewModel.addExpense(desc, amount, categoryId, date, targetLekkaId = lastRoute.lekkaId)
                                                }
                                                backStack = backStack.filterNot { it is Route.AddExpense }
                                            },
                                            onDelete = {
                                                expenseItem?.let { viewModel.deleteExpense(it.expense) }
                                                backStack = backStack.filterNot { it is Route.AddExpense }
                                            },
                                            onBack = {
                                                backStack = backStack.filterNot { it is Route.AddExpense }
                                            },
                                            showBackButton = false
                                        )
                                    }
                                    is Route.CategoryManagement -> {
                                        CategoryManagementScreen(
                                            categories = categories,
                                            onAddCategory = viewModel::addCategory,
                                            onUpdateCategory = viewModel::updateCategory,
                                            onDeleteCategory = viewModel::deleteCategory,
                                            onBack = {
                                                backStack = backStack.filterNot { it is Route.CategoryManagement }
                                            }
                                        )
                                    }
                                    is Route.Analytics -> {
                                        AnalyticsScreen(
                                            summaries = monthlySummaries,
                                            expenses = expenses.map { it.toExpenseWithCategory() },
                                            onBack = {
                                                backStack = backStack.filterNot { it is Route.Analytics }
                                            }
                                        )
                                    }
                                    else -> {
                                        Surface(
                                            modifier = Modifier.fillMaxSize(),
                                            color = MaterialTheme.colorScheme.background
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    "Select an action",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                } else {
                    NavDisplay(
                        backStack = backStack,
                        onBack = { 
                            if (backStack.size > 1) backStack = backStack.dropLast(1)
                        },
                        entryProvider = { key ->
                            when (key) {
                                is Route.Welcome -> NavEntry(key) {
                                    WelcomeScreen(
                                        lekkas = allLekkasWithSummary,
                                        selectedLekkaId = selectedLekkaId,
                                        mostRecentSheet = mostRecentSheet,
                                        motherTableSummary = motherTableSummary,
                                        recentExpenses = expenses,
                                        onLekkaSelected = viewModel::selectLekka,
                                        onQuickAddClick = { targetId ->
                                            viewModel.selectLekka(targetId)
                                            backStack = backStack + Route.AddExpense(targetId)
                                        },
                                        onSeeQuickNotesClick = { id ->
                                            viewModel.selectLekka(id)
                                            backStack = backStack + Route.TableDetail(id)
                                        },
                                        onManageAllTablesClick = {
                                            backStack = backStack + Route.Tables
                                        }
                                    )
                                }
                                is Route.Tables -> NavEntry(key) {
                                    TablesScreen(
                                        lekkas = allLekkasWithSummary,
                                        motherTableSummary = motherTableSummary,
                                        onBack = {
                                            if (backStack.size > 1) backStack = backStack.dropLast(1)
                                        },
                                        onTableClick = { lekkaId ->
                                            viewModel.selectLekka(lekkaId)
                                            backStack = backStack + Route.TableDetail(lekkaId)
                                        },
                                        onSetDefaultLekka = viewModel::setDefaultLekka,
                                        onCreateLekka = { name, startDate, endDate, isDefault, categories ->
                                            viewModel.addLekka(name, startDate, endDate, isDefault, categories)
                                        },
                                        onUpdateLekka = { lekka, isDefault, categories ->
                                            viewModel.updateLekka(lekka, isDefault, categories)
                                        },
                                        onDeleteLekka = viewModel::deleteLekka
                                    )
                                }
                                is Route.TableDetail -> NavEntry(key) {
                                    val currentLekkaWithSummary = allLekkasWithSummary.find { it.lekka.id == key.lekkaId }
                                    val currentLekkaName = currentLekkaWithSummary?.lekka?.name ?: "Unknown"
                                    val isMother = currentLekkaWithSummary?.lekka?.isMotherTable == true

                                    TableDetailScreen(
                                        lekkaName = currentLekkaName,
                                        isMotherTable = isMother,
                                        childLekkas = childLekkas,
                                        expenses = expenses,
                                        monthlySummaries = monthlySummaries,
                                        categories = categories,
                                        onBack = {
                                            if (backStack.size > 1) backStack = backStack.dropLast(1)
                                        },
                                        onAddExpenseClick = { targetId ->
                                            backStack = backStack + Route.AddExpense(targetId)
                                        },
                                        onExpenseClick = { item ->
                                            backStack = backStack + Route.AddExpense(item.expense.lekkaId, item.expense.id)
                                        },
                                        onDeleteExpense = { viewModel.deleteExpense(it.expense) },
                                        onDeleteExpenses = { list -> viewModel.deleteExpenses(list.map { it.expense }) },
                                        onManageCategoriesClick = {
                                            backStack = backStack + Route.CategoryManagement(key.lekkaId)
                                        }
                                    )
                                }
                                is Route.Dashboard -> NavEntry(key) {
                                    val currentLekkaWithSummary = allLekkasWithSummary.find { it.lekka.id == key.lekkaId }
                                    val currentLekkaName = currentLekkaWithSummary?.lekka?.name ?: "Unknown"
                                    val isMother = currentLekkaWithSummary?.lekka?.isMotherTable == true

                                    TableDetailScreen(
                                        lekkaName = currentLekkaName,
                                        isMotherTable = isMother,
                                        childLekkas = childLekkas,
                                        expenses = expenses,
                                        monthlySummaries = monthlySummaries,
                                        categories = categories,
                                        onBack = {
                                            if (backStack.size > 1) backStack = backStack.dropLast(1)
                                        },
                                        onAddExpenseClick = { targetId ->
                                            backStack = backStack + Route.AddExpense(targetId)
                                        },
                                        onExpenseClick = { item ->
                                            backStack = backStack + Route.AddExpense(item.expense.lekkaId, item.expense.id)
                                        },
                                        onDeleteExpense = { viewModel.deleteExpense(it.expense) },
                                        onDeleteExpenses = { list -> viewModel.deleteExpenses(list.map { it.expense }) },
                                        onManageCategoriesClick = {
                                            backStack = backStack + Route.CategoryManagement(key.lekkaId)
                                        }
                                    )
                                }
                                is Route.AddExpense -> NavEntry(key) {
                                    val expenseItem = expenses.find { it.expense.id == key.expenseId }
                                    AddExpenseScreen(
                                        categories = categories,
                                        expenseToEdit = expenseItem?.toExpenseWithCategory(),
                                        onSave = { id, desc, amount, categoryId, date ->
                                            if (id != null) {
                                                viewModel.updateExpense(id, desc, amount, categoryId, date, targetLekkaId = key.lekkaId)
                                            } else {
                                                viewModel.addExpense(desc, amount, categoryId, date, targetLekkaId = key.lekkaId)
                                            }
                                            if (backStack.size > 1) backStack = backStack.dropLast(1)
                                        },
                                        onDelete = {
                                            expenseItem?.let { viewModel.deleteExpense(it.expense) }
                                            if (backStack.size > 1) backStack = backStack.dropLast(1)
                                        },
                                        onBack = {
                                            if (backStack.size > 1) backStack = backStack.dropLast(1)
                                        }
                                    )
                                }
                                is Route.CategoryManagement -> NavEntry(key) {
                                    CategoryManagementScreen(
                                        categories = categories,
                                        onAddCategory = viewModel::addCategory,
                                        onUpdateCategory = viewModel::updateCategory,
                                        onDeleteCategory = viewModel::deleteCategory,
                                        onBack = {
                                            if (backStack.size > 1) backStack = backStack.dropLast(1)
                                        }
                                    )
                                }
                                is Route.Analytics -> NavEntry(key) {
                                    AnalyticsScreen(
                                        summaries = monthlySummaries,
                                        expenses = expenses.map { it.toExpenseWithCategory() },
                                        onBack = {
                                            if (backStack.size > 1) backStack = backStack.dropLast(1)
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
