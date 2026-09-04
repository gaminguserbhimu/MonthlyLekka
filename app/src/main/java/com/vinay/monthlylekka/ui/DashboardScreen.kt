package com.vinay.monthlylekka.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.Expense
import com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka
import com.vinay.monthlylekka.data.Lekka
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    lekkaName: String,
    isMotherTable: Boolean = false,
    childLekkas: List<Lekka> = emptyList(),
    expenses: List<ExpenseWithCategoryAndLekka>,
    onAddExpenseClick: (Long) -> Unit,
    onManageCategoriesClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onExpenseClick: (ExpenseWithCategoryAndLekka) -> Unit,
    onDeleteExpense: (ExpenseWithCategoryAndLekka) -> Unit,
    onDeleteExpenses: ((List<ExpenseWithCategoryAndLekka>) -> Unit)? = null,
    onSwitchLekka: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSelectChildDialog by remember { mutableStateOf(false) }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedExpenseIds by remember { mutableStateOf(setOf<Long>()) }

    val totalIncome = expenses.filter { it.category.isIncome }.sumOf { it.expense.amount }
    val totalExpense = expenses.filter { !it.category.isIncome }.sumOf { it.expense.amount }
    val netBalance = totalIncome - totalExpense

    Scaffold(
        modifier = modifier,
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedExpenseIds.size} selected",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedExpenseIds = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Selection Mode")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (selectedExpenseIds.size == expenses.size) {
                                selectedExpenseIds = emptySet()
                                isSelectionMode = false
                            } else {
                                selectedExpenseIds = expenses.map { it.expense.id }.toSet()
                            }
                        }) {
                            Icon(
                                imageVector = if (selectedExpenseIds.size == expenses.size) Icons.Rounded.Deselect else Icons.Rounded.SelectAll,
                                contentDescription = if (selectedExpenseIds.size == expenses.size) "Deselect All" else "Select All"
                            )
                        }
                        IconButton(
                            onClick = {
                                val selectedList = expenses.filter { it.expense.id in selectedExpenseIds }
                                if (onDeleteExpenses != null) {
                                    onDeleteExpenses(selectedList)
                                } else {
                                    selectedList.forEach { onDeleteExpense(it) }
                                }
                                isSelectionMode = false
                                selectedExpenseIds = emptySet()
                            },
                            enabled = selectedExpenseIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Batch Delete Selected",
                                tint = if (selectedExpenseIds.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isMotherTable) "👑 $lekkaName" else lekkaName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (isMotherTable) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "MASTER EXPENSE TABLE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isMotherTable) "Aggregated View (Read-Only Direct Entry)" else "Dashboard",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onSwitchLekka) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Switch Expense Table")
                        }
                    },
                    actions = {
                        IconButton(onClick = onAnalyticsClick) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Analytics")
                        }
                        if (!isMotherTable) {
                            IconButton(onClick = onManageCategoriesClick) {
                                Icon(Icons.Rounded.Category, contentDescription = "Manage Categories")
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                LargeFloatingActionButton(
                    onClick = {
                        if (isMotherTable) {
                            showSelectChildDialog = true
                        } else {
                            childLekkas.firstOrNull()?.id?.let { onAddExpenseClick(it) } ?: onAddExpenseClick(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Entry")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            BalanceCard(netBalance, totalIncome, totalExpense)
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isMotherTable) "All Aggregated Transactions" else "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (isSelectionMode) {
                    Text(
                        text = "${selectedExpenseIds.size} Selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ExpenseList(
                expenses = expenses,
                isMotherTable = isMotherTable,
                isSelectionMode = isSelectionMode,
                selectedExpenseIds = selectedExpenseIds,
                onToggleSelect = { id ->
                    selectedExpenseIds = if (id in selectedExpenseIds) {
                        selectedExpenseIds - id
                    } else {
                        selectedExpenseIds + id
                    }
                    if (selectedExpenseIds.isEmpty()) {
                        isSelectionMode = false
                    }
                },
                onLongPressSelect = { id ->
                    isSelectionMode = true
                    selectedExpenseIds = selectedExpenseIds + id
                },
                onExpenseClick = onExpenseClick,
                onDeleteExpense = onDeleteExpense
            )
        }
    }

    if (showSelectChildDialog) {
        SelectChildTableDialog(
            childLekkas = childLekkas,
            onDismiss = { showSelectChildDialog = false },
            onChildTableSelected = { selectedChildId ->
                showSelectChildDialog = false
                onAddExpenseClick(selectedChildId)
            }
        )
    }
}

@Composable
fun BalanceCard(netBalance: Double, income: Double, expense: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem("Total Income", income, MaterialTheme.colorScheme.primary)
                SummaryItem("Total Expenses", expense, MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = netBalance.toCurrencyString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (netBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: Double, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = amount.toCurrencyString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ExpenseList(
    expenses: List<ExpenseWithCategoryAndLekka>,
    isMotherTable: Boolean,
    isSelectionMode: Boolean,
    selectedExpenseIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onLongPressSelect: (Long) -> Unit,
    onExpenseClick: (ExpenseWithCategoryAndLekka) -> Unit,
    onDeleteExpense: (ExpenseWithCategoryAndLekka) -> Unit
) {
    if (expenses.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No transactions found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(expenses, key = { it.expense.id }) { item ->
                ExpenseListItem(
                    item = item,
                    isMotherTable = isMotherTable,
                    isSelectionMode = isSelectionMode,
                    isSelected = selectedExpenseIds.contains(item.expense.id),
                    onToggleSelect = { onToggleSelect(item.expense.id) },
                    onLongPressSelect = { onLongPressSelect(item.expense.id) },
                    onExpenseClick = onExpenseClick,
                    onDeleteExpense = onDeleteExpense
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseListItem(
    item: ExpenseWithCategoryAndLekka,
    isMotherTable: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongPressSelect: () -> Unit,
    onExpenseClick: (ExpenseWithCategoryAndLekka) -> Unit,
    onDeleteExpense: (ExpenseWithCategoryAndLekka) -> Unit
) {
    val expense = item.expense
    val category = item.category

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelect()
                    } else {
                        onExpenseClick(item)
                    }
                },
                onLongClick = {
                    if (!isSelectionMode) {
                        onLongPressSelect()
                    } else {
                        onToggleSelect()
                    }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Checkbox + Date Badge + Category Avatar & Badge + Origin Tag + Delete button (when not in selection mode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() }
                    )
                }

                // Date Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = expense.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Category Badge
                CategoryIconAvatar(
                    categoryName = category.name,
                    colorHex = category.colorHex,
                    isIncome = category.isIncome,
                    size = 28.dp,
                    iconSize = 16.dp
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Origin Tag Pill (if Mother Table)
                if (isMotherTable && item.lekkaName.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "[${item.lekkaName}]",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!isSelectionMode) {
                    IconButton(
                        onClick = { onDeleteExpense(item) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Transaction",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Description & Right-Aligned Cost Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = expense.description.ifBlank { "No description" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "${if (category.isIncome) "+" else "-"} ${expense.amount.toCurrencyString()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (category.isIncome) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun SelectChildTableDialog(
    childLekkas: List<Lekka>,
    onDismiss: () -> Unit,
    onChildTableSelected: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Expense Table", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Direct entry into Master Expense Table is read-only. Please select an Expense Table to record this transaction:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (childLekkas.isEmpty()) {
                    Text(
                        "No Expense Tables available. Please create an Expense Table first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        items(childLekkas) { lekka ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onChildTableSelected(lekka.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lekka.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (lekka.isDefault) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981)
                                        ) {
                                            Text(
                                                text = "DEFAULT",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    val dummyCategories = listOf(
        Category(id = 1, lekkaId = 1, name = "Income", colorHex = "#2E7D32", isIncome = true),
        Category(id = 2, lekkaId = 1, name = "Kirani", colorHex = "#FFB300", isIncome = false)
    )
    val dummyExpenses = listOf(
        ExpenseWithCategoryAndLekka(Expense(id = 1, lekkaId = 1, description = "Salary", amount = 50000.0, categoryId = 1, date = LocalDate.now()), dummyCategories[0], "Monthly Lekka"),
        ExpenseWithCategoryAndLekka(Expense(id = 2, lekkaId = 2, description = "Kirani Items", amount = 1200.0, categoryId = 2, date = LocalDate.now()), dummyCategories[1], "Goa Trip")
    )
    MonthlyLekkaTheme {
        DashboardScreen(
            lekkaName = "Master Lekka",
            isMotherTable = true,
            childLekkas = emptyList(),
            expenses = dummyExpenses,
            onAddExpenseClick = {},
            onManageCategoriesClick = {},
            onAnalyticsClick = {},
            onExpenseClick = {},
            onDeleteExpense = {},
            onSwitchLekka = {}
        )
    }
}
