package com.vinay.monthlylekka.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.Expense
import com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka
import com.vinay.monthlylekka.data.Lekka
import com.vinay.monthlylekka.data.MonthlySummary
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class YearlySummary(
    val year: String,
    val totalIncome: Double,
    val totalExpense: Double
) {
    val netBalance: Double get() = totalIncome - totalExpense
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDetailScreen(
    lekkaName: String,
    isMotherTable: Boolean,
    childLekkas: List<Lekka>,
    expenses: List<ExpenseWithCategoryAndLekka>,
    monthlySummaries: List<MonthlySummary>,
    categories: List<Category>,
    onBack: () -> Unit,
    onAddExpenseClick: (Long) -> Unit,
    onExpenseClick: (ExpenseWithCategoryAndLekka) -> Unit,
    onDeleteExpense: (ExpenseWithCategoryAndLekka) -> Unit,
    onDeleteExpenses: ((List<ExpenseWithCategoryAndLekka>) -> Unit)? = null,
    onManageCategoriesClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })
    var showSelectChildDialog by remember { mutableStateOf(false) }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedExpenseIds by remember { mutableStateOf(setOf<Long>()) }

    // Clear selection when navigating away from Slide 0
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 0 && isSelectionMode) {
            isSelectionMode = false
            selectedExpenseIds = emptySet()
        }
    }

    val tabs = listOf(
        "Transactions" to Icons.AutoMirrored.Rounded.List,
        "Pie Charts" to Icons.Rounded.PieChart,
        "Monthly Table" to Icons.Rounded.CalendarMonth,
        "Yearly Charts" to Icons.Rounded.Analytics
    )

    Scaffold(
        topBar = {
            if (pagerState.currentPage == 0 && isSelectionMode) {
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
                                imageVector = Icons.Rounded.Delete,
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
                                    text = lekkaName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isMotherTable) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF59E0B)
                                    ) {
                                        Text(
                                            text = "👑 MASTER EXPENSE SHEET",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isMotherTable) "Aggregated Overview across all expense sheets" else "Expense Sheet Details & Analytics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (!isMotherTable) {
                            IconButton(onClick = onManageCategoriesClick) {
                                Icon(Icons.Rounded.Category, contentDescription = "Manage Categories")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0 && !isSelectionMode) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (isMotherTable) {
                            showSelectChildDialog = true
                        } else {
                            val currentLekkaId = expenses.firstOrNull()?.expense?.lekkaId ?: 1L
                            onAddExpenseClick(currentLekkaId)
                        }
                    },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = {
                        Text(
                            if (isMotherTable) "Add Expense (Select Expense Sheet)" else "Add Expense",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium) },
                        icon = { Icon(icon, contentDescription = title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> TransactionsSlide(
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
                    1 -> PieChartsSlide(
                        expenses = expenses
                    )
                    2 -> MonthlyTableSlide(
                        expenses = expenses,
                        summaries = monthlySummaries
                    )
                    3 -> YearlyChartsSlide(
                        expenses = expenses,
                        monthlySummaries = monthlySummaries
                    )
                }
            }
        }
    }

    if (showSelectChildDialog) {
        SelectChildTableDialogForDetail(
            childLekkas = childLekkas,
            onDismiss = { showSelectChildDialog = false },
            onChildTableSelected = { selectedId: Long ->
                showSelectChildDialog = false
                onAddExpenseClick(selectedId)
            }
        )
    }
}

// ---------------------------------------------------------------------------
// SLIDE 1: TRANSACTIONS LIST
// Clean transaction rows without arrows
// ---------------------------------------------------------------------------
@Composable
fun TransactionsSlide(
    expenses: List<ExpenseWithCategoryAndLekka>,
    isMotherTable: Boolean,
    isSelectionMode: Boolean,
    selectedExpenseIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onLongPressSelect: (Long) -> Unit,
    onExpenseClick: (ExpenseWithCategoryAndLekka) -> Unit,
    onDeleteExpense: (ExpenseWithCategoryAndLekka) -> Unit,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No transactions recorded yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Click the '+' button below to add your first expense.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Transactions: ${expenses.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
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
            }
        }

        items(expenses, key = { it.expense.id }) { item ->
            TransactionRowFormatted(
                item = item,
                isMotherTable = isMotherTable,
                isSelectionMode = isSelectionMode,
                isSelected = selectedExpenseIds.contains(item.expense.id),
                onToggleSelect = { onToggleSelect(item.expense.id) },
                onLongPressSelect = { onLongPressSelect(item.expense.id) },
                onClick = { onExpenseClick(item) },
                onDelete = { onDeleteExpense(item) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRowFormatted(
    item: ExpenseWithCategoryAndLekka,
    isMotherTable: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongPressSelect: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val expense = item.expense
    val category = item.category
    val formattedDate = expense.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    val categoryColor = try {
        Color(category.colorHex.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelect()
                    } else {
                        onClick()
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Checkbox + Date Badge + Category Badge + Origin Tag
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
                        text = formattedDate,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Category Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = categoryColor,
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
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!isSelectionMode) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                val amountText = "${if (category.isIncome) "+" else "-"} ₹${String.format(Locale.getDefault(), "%,.2f", expense.amount)}"
                val amountColor = if (category.isIncome) Color(0xFF10B981) else Color(0xFFEF4444)

                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = amountColor,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SLIDE 2: PIE CHARTS (Overall & Down Monthly)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PieChartsSlide(
    expenses: List<ExpenseWithCategoryAndLekka>,
    modifier: Modifier = Modifier
) {
    val expenseOnlyList = expenses.filter { !it.category.isIncome }
    
    val availableMonths = remember(expenses) {
        expenses.map { it.expense.date.format(DateTimeFormatter.ofPattern("yyyy-MM")) }
            .distinct()
            .sortedDescending()
    }

    var selectedMonth by remember(availableMonths) {
        mutableStateOf(availableMonths.firstOrNull() ?: YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Overall Expense Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            SinglePieChartCard(
                title = "Overall Expense Breakdown",
                expenses = expenseOnlyList
            )
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Expense Distribution",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (availableMonths.isNotEmpty()) {
                        MonthSelectorDropdown(
                            availableMonths = availableMonths,
                            selectedMonth = selectedMonth,
                            onMonthSelected = { selectedMonth = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val monthlyExpenses = remember(expenseOnlyList, selectedMonth) {
                    expenseOnlyList.filter {
                        it.expense.date.format(DateTimeFormatter.ofPattern("yyyy-MM")) == selectedMonth
                    }
                }

                val formattedMonthLabel = try {
                    YearMonth.parse(selectedMonth).format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                } catch (_: Exception) {
                    selectedMonth
                }

                SinglePieChartCard(
                    title = "Expenses for $formattedMonthLabel",
                    expenses = monthlyExpenses
                )
            }
        }
    }
}

@Composable
fun MonthSelectorDropdown(
    availableMonths: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedSelected = try {
        YearMonth.parse(selectedMonth).format(DateTimeFormatter.ofPattern("MMM yyyy"))
    } catch (_: Exception) {
        selectedMonth
    }

    Box {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text(formattedSelected, fontWeight = FontWeight.Bold) },
            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableMonths.forEach { month ->
                val label = try {
                    YearMonth.parse(month).format(DateTimeFormatter.ofPattern("MMM yyyy"))
                } catch (_: Exception) {
                    month
                }
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SinglePieChartCard(
    title: String,
    expenses: List<ExpenseWithCategoryAndLekka>
) {
    val groupedData = remember(expenses) {
        expenses.groupBy { it.category.name }
            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }
    }

    val totalAmount = groupedData.values.sum()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (groupedData.isEmpty() || totalAmount <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded for this selection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val categoryColors = remember(expenses) {
                    expenses.associateBy(
                        { it.category.name },
                        {
                            try {
                                Color(it.category.colorHex.toColorInt())
                            } catch (_: Exception) {
                                Color.Gray
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        groupedData.forEach { (categoryName, amount) ->
                            val sweepAngle = ((amount / totalAmount) * 360f).toFloat()
                            drawArc(
                                color = categoryColors[categoryName] ?: Color.Gray,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${"%.0f".format(totalAmount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groupedData.forEach { (categoryName, amount) ->
                        val percent = (amount / totalAmount * 100)
                        val color = categoryColors[categoryName] ?: Color.Gray
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$categoryName: ₹${"%.0f".format(amount)} (${"%.1f".format(percent)}%)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SLIDE 3: MONTHLY CATEGORY BREAKDOWN TABLE
// Columns: Month | Category | Total Cost
// Grouped by month with Subtotal row at bottom of each month
// ---------------------------------------------------------------------------
@Composable
fun MonthlyTableSlide(
    expenses: List<ExpenseWithCategoryAndLekka>,
    summaries: List<MonthlySummary>,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No monthly summary data available.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val groupedByMonth = remember(expenses) {
        expenses.groupBy { YearMonth.from(it.expense.date) }
            .toSortedMap(compareByDescending { it })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Monthly Financial Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Category Breakdown Table grouped by month
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(14.dp)
                    ) {
                        TableCell(text = "Month", weight = 1.1f, isHeader = true)
                        TableCell(text = "Category", weight = 1.1f, isHeader = true)
                        TableCell(text = "Total Cost", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                    }

                    groupedByMonth.forEach { (yearMonth, monthExpenses) ->
                        val formattedMonth = yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"))

                        val categoryMap = monthExpenses.groupBy { it.category.name }
                            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }
                            .toSortedMap()

                        val monthSubtotal = categoryMap.values.sum()

                        categoryMap.entries.forEachIndexed { index, (catName, totalCost) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(
                                    text = if (index == 0) formattedMonth else "",
                                    weight = 1.1f,
                                    fontWeight = FontWeight.Bold
                                )
                                TableCell(text = catName, weight = 1.1f)
                                TableCell(
                                    text = "₹${String.format(Locale.getDefault(), "%,.2f", totalCost)}",
                                    weight = 1.2f,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }

                        // Month Subtotal Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(
                                text = "$formattedMonth Total",
                                weight = 2.2f,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            TableCell(
                                text = "₹${String.format(Locale.getDefault(), "%,.2f", monthSubtotal)}",
                                weight = 1.2f,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Monthly Income & Expense Overview
        if (summaries.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Monthly Income & Expense Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(14.dp)
                        ) {
                            TableCell(text = "Month", weight = 1.1f, isHeader = true)
                            TableCell(text = "Income", weight = 1.1f, isHeader = true, textAlign = TextAlign.End)
                            TableCell(text = "Expenses", weight = 1.1f, isHeader = true, textAlign = TextAlign.End)
                            TableCell(text = "Net Balance", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                        }

                        summaries.forEach { summary ->
                            val formattedMonth = try {
                                YearMonth.parse(summary.month).format(DateTimeFormatter.ofPattern("MMM yyyy"))
                            } catch (_: Exception) {
                                summary.month
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(text = formattedMonth, weight = 1.1f)
                                TableCell(text = "₹${"%.0f".format(summary.totalIncome)}", weight = 1.1f, color = Color(0xFF10B981), textAlign = TextAlign.End)
                                TableCell(text = "₹${"%.0f".format(summary.totalExpense)}", weight = 1.1f, color = Color(0xFFEF4444), textAlign = TextAlign.End)
                                TableCell(
                                    text = "₹${"%.0f".format(summary.netBalance)}",
                                    weight = 1.2f,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    color = if (summary.netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }

                        val grandTotalIncome = summaries.sumOf { it.totalIncome }
                        val grandTotalExpense = summaries.sumOf { it.totalExpense }
                        val grandNetBalance = grandTotalIncome - grandTotalExpense

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(text = "TOTAL", weight = 1.1f, fontWeight = FontWeight.ExtraBold)
                            TableCell(text = "₹${"%.0f".format(grandTotalIncome)}", weight = 1.1f, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            TableCell(text = "₹${"%.0f".format(grandTotalExpense)}", weight = 1.1f, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            TableCell(
                                text = "₹${"%.0f".format(grandNetBalance)}",
                                weight = 1.2f,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.End,
                                color = if (grandNetBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SLIDE 4: YEARLY CHARTS & CATEGORY BREAKDOWN TABLE
// Columns: Year | Category | Total Cost
// Grouped by year with Subtotal row at bottom of each year
// ---------------------------------------------------------------------------
@Composable
fun YearlyChartsSlide(
    expenses: List<ExpenseWithCategoryAndLekka>,
    monthlySummaries: List<MonthlySummary>,
    modifier: Modifier = Modifier
) {
    val yearlySummaries = remember(monthlySummaries) {
        monthlySummaries
            .groupBy { it.month.take(4) }
            .map { (year, list) ->
                YearlySummary(
                    year = year,
                    totalIncome = list.sumOf { it.totalIncome },
                    totalExpense = list.sumOf { it.totalExpense }
                )
            }
            .sortedByDescending { it.year }
    }

    val availableYears = remember(yearlySummaries) {
        yearlySummaries.map { it.year }.ifEmpty { listOf(LocalDate.now().year.toString()) }
    }

    var selectedYear by remember(availableYears) {
        mutableStateOf(availableYears.first())
    }

    val expenseOnlyList = remember(expenses) {
        expenses.filter { !it.category.isIncome }
    }

    val yearlyExpenses = remember(expenseOnlyList, selectedYear) {
        expenseOnlyList.filter {
            it.expense.date.year.toString() == selectedYear
        }
    }

    val groupedByYear = remember(expenses) {
        expenses.groupBy { it.expense.date.year.toString() }
            .toSortedMap(compareByDescending { it })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Yearly Pie Chart
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Yearly Expense Pie Chart",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (availableYears.size > 1) {
                        YearSelectorDropdown(
                            availableYears = availableYears,
                            selectedYear = selectedYear,
                            onYearSelected = { selectedYear = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SinglePieChartCard(
                    title = "Expense Breakdown for Year $selectedYear",
                    expenses = yearlyExpenses
                )
            }
        }

        // 2. Yearly Category Breakdown Table
        item {
            Column {
                Text(
                    text = "Yearly Category Breakdown Table",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (groupedByYear.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No yearly category data available.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(14.dp)
                            ) {
                                TableCell(text = "Year", weight = 1f, isHeader = true)
                                TableCell(text = "Category", weight = 1.1f, isHeader = true)
                                TableCell(text = "Total Cost", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                            }

                            groupedByYear.forEach { (yearStr, yearExpenses) ->
                                val categoryMap = yearExpenses.groupBy { it.category.name }
                                    .mapValues { entry -> entry.value.sumOf { it.expense.amount } }
                                    .toSortedMap()

                                val yearSubtotal = categoryMap.values.sum()

                                categoryMap.entries.forEachIndexed { index, (catName, totalCost) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableCell(
                                            text = if (index == 0) yearStr else "",
                                            weight = 1f,
                                            fontWeight = FontWeight.Bold
                                        )
                                        TableCell(text = catName, weight = 1.1f)
                                        TableCell(
                                            text = "₹${String.format(Locale.getDefault(), "%,.2f", totalCost)}",
                                            weight = 1.2f,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                }

                                // Year Subtotal Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCell(
                                        text = "$yearStr Total",
                                        weight = 2.1f,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    TableCell(
                                        text = "₹${String.format(Locale.getDefault(), "%,.2f", yearSubtotal)}",
                                        weight = 1.2f,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.End,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Yearly Income/Expense Summary Table
        item {
            Column {
                Text(
                    text = "Yearly Summary Table",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (yearlySummaries.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No yearly summary data available.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(14.dp)
                            ) {
                                TableCell(text = "Year", weight = 1f, isHeader = true)
                                TableCell(text = "Total Income", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                                TableCell(text = "Total Expenses", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                                TableCell(text = "Net Balance", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                            }

                            yearlySummaries.forEach { summary ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCell(text = summary.year, weight = 1f, fontWeight = FontWeight.Bold)
                                    TableCell(text = "₹${"%.0f".format(summary.totalIncome)}", weight = 1.2f, color = Color(0xFF10B981), textAlign = TextAlign.End)
                                    TableCell(text = "₹${"%.0f".format(summary.totalExpense)}", weight = 1.2f, color = Color(0xFFEF4444), textAlign = TextAlign.End)
                                    TableCell(
                                        text = "₹${"%.0f".format(summary.netBalance)}",
                                        weight = 1.2f,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        color = if (summary.netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }

                            val grandIncome = yearlySummaries.sumOf { it.totalIncome }
                            val grandExpense = yearlySummaries.sumOf { it.totalExpense }
                            val grandNet = grandIncome - grandExpense

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(text = "GRAND TOTAL", weight = 1f, fontWeight = FontWeight.ExtraBold)
                                TableCell(text = "₹${"%.0f".format(grandIncome)}", weight = 1.2f, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                TableCell(text = "₹${"%.0f".format(grandExpense)}", weight = 1.2f, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                TableCell(
                                    text = "₹${"%.0f".format(grandNet)}",
                                    weight = 1.2f,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.End,
                                    color = if (grandNet >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YearSelectorDropdown(
    availableYears: List<String>,
    selectedYear: String,
    onYearSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text(selectedYear, fontWeight = FontWeight.Bold) },
            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableYears.forEach { yr ->
                DropdownMenuItem(
                    text = { Text(yr) },
                    onClick = {
                        onYearSelected(yr)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SelectChildTableDialogForDetail(
    childLekkas: List<Lekka>,
    onDismiss: () -> Unit,
    onChildTableSelected: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Expense Sheet to Add Expense",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            if (childLekkas.isEmpty()) {
                Text("No expense sheets found. Please create an expense sheet first.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose which expense sheet/event this expense belongs to:")
                    childLekkas.forEach { child ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChildTableSelected(child.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = child.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun TableDetailScreenPreview() {
    val dummyCategory = Category(id = 1, lekkaId = 1, name = "Kirani", colorHex = "#FFB300", isIncome = false)
    val dummyExpense = ExpenseWithCategoryAndLekka(
        expense = Expense(id = 1, lekkaId = 1, description = "Milk & Rice", amount = 450.0, categoryId = 1, date = LocalDate.now()),
        category = dummyCategory,
        lekkaName = "Goa Trip"
    )

    MonthlyLekkaTheme {
        TableDetailScreen(
            lekkaName = "Master Lekka",
            isMotherTable = true,
            childLekkas = listOf(Lekka(id = 2, name = "Goa Trip", isMotherTable = false)),
            expenses = listOf(dummyExpense),
            monthlySummaries = listOf(MonthlySummary("2026-09", 50000.0, 12000.0)),
            categories = listOf(dummyCategory),
            onBack = {},
            onAddExpenseClick = {},
            onExpenseClick = {},
            onDeleteExpense = {}
        )
    }
}
