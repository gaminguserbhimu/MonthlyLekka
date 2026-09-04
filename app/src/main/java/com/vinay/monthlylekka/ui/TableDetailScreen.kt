package com.vinay.monthlylekka.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.launch

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
                                            text = "👑 MASTER EXPENSE TABLE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isMotherTable) "Aggregated Overview across all expense tables" else "Expense Table Details & Analytics",
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
                            if (isMotherTable) "Add Expense (Select Expense Table)" else "Add Expense",
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
                    val totalIncome = remember(expenses) {
                        expenses.filter { it.category.isIncome }.sumOf { it.expense.amount }
                    }
                    val totalOutcome = remember(expenses) {
                        expenses.filter { !it.category.isIncome }.sumOf { it.expense.amount }
                    }
                    val netBalance = totalIncome - totalOutcome
                    val netSign = if (netBalance >= 0) "+" else "-"
                    val netColor = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    val absNet = if (netBalance < 0) -netBalance else netBalance
                    val netText = "$netSign ${absNet.toCurrencyString()}"

                    // Left Side: Transaction Count with Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = "Transaction Count",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isSelectionMode) "${selectedExpenseIds.size} Selected" else "${expenses.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Right Side: Total Income, Total Outcome & Net Balance (Right-Aligned)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Income Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                contentDescription = "Total Income",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = totalIncome.toCurrencyString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        // Outcome Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                                contentDescription = "Total Outcome",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = totalOutcome.toCurrencyString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }

                        // Net Balance Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (netBalance >= 0) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                                contentDescription = "Net Balance",
                                tint = netColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = netText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = netColor
                            )
                        }
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

@Composable
fun TransactionRowFormatted(
    item: ExpenseWithCategoryAndLekka,
    isMotherTable: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongPressSelect: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    ExpenseItemCard(
        item = item,
        isMotherTable = isMotherTable,
        isSelectionMode = isSelectionMode,
        isSelected = isSelected,
        onToggleSelect = onToggleSelect,
        onLongPressSelect = onLongPressSelect,
        onExpenseClick = { onClick() }
    )
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

    var selectedCategoryName by remember(groupedData) {
        mutableStateOf<String?>(groupedData.keys.firstOrNull())
    }

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
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(groupedData, totalAmount) {
                                detectTapGestures { tapOffset ->
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val dx = tapOffset.x - centerX
                                    val dy = tapOffset.y - centerY
                                    val distance = sqrt(dx * dx + dy * dy)
                                    val maxRadius = min(size.width, size.height) / 2f

                                    if (distance <= maxRadius && totalAmount > 0) {
                                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        if (angle < 0) angle += 360f
                                        val touchAngle = (angle - 270f + 360f) % 360f

                                        var currentSweep = 0f
                                        for ((catName, amount) in groupedData) {
                                            val sweep = ((amount / totalAmount) * 360f).toFloat()
                                            if (touchAngle >= currentSweep && touchAngle <= currentSweep + sweep) {
                                                selectedCategoryName = catName
                                                break
                                            }
                                            currentSweep += sweep
                                        }
                                    }
                                }
                            }
                    ) {
                        var startAngle = -90f
                        groupedData.forEach { (categoryName, amount) ->
                            val sweepAngle = ((amount / totalAmount) * 360f).toFloat()
                            val isSelected = (categoryName == selectedCategoryName)
                            val color = categoryColors[categoryName] ?: Color.Gray

                            if (isSelected) {
                                val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                                val offsetDistance = 10.dp.toPx()
                                val dx = (cos(midAngleRad) * offsetDistance).toFloat()
                                val dy = (sin(midAngleRad) * offsetDistance).toFloat()

                                translate(left = dx, top = dy) {
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true
                                    )
                                }
                            } else {
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true
                                )
                            }
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
                                    text = totalAmount.toCurrencyString(),
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
                        val isSelected = (categoryName == selectedCategoryName)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedCategoryName = categoryName }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 14.dp else 12.dp)
                                    .background(color, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$categoryName: ${amount.toCurrencyString()} (${"%.1f".format(percent)}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Interactive Category Details Card
                if (selectedCategoryName != null) {
                    val selAmount = groupedData[selectedCategoryName] ?: 0.0
                    val percent = if (totalAmount > 0) (selAmount / totalAmount * 100) else 0.0
                    val selColor = categoryColors[selectedCategoryName] ?: Color.Gray

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = selColor.copy(alpha = 0.12f)),
                        border = BorderStroke(1.5.dp, selColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(selColor, CircleShape)
                                )
                                Column {
                                    Text(
                                        text = selectedCategoryName ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Selected Category",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = selAmount.toCurrencyString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = selColor.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "${"%.1f".format(percent)}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Monthly Financial Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Calculation Breakdown Card & Table per Month
        items(groupedByMonth.entries.toList(), key = { it.key }) { (yearMonth, monthExpenses) ->
            val formattedMonth = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

            val incomeExpenses = monthExpenses.filter { it.category.isIncome }
            val expenseExpenses = monthExpenses.filter { !it.category.isIncome }

            val totalIncome = incomeExpenses.sumOf { it.expense.amount }
            val totalExpense = expenseExpenses.sumOf { it.expense.amount }
            val netBalance = totalIncome - totalExpense

            val incomeCategories = incomeExpenses.groupBy { it.category.name }
                .mapValues { entry -> entry.value.sumOf { it.expense.amount } }
                .toSortedMap()

            val expenseCategories = expenseExpenses.groupBy { it.category.name }
                .mapValues { entry -> entry.value.sumOf { it.expense.amount } }
                .toSortedMap()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column {
                    // Month Title Header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = formattedMonth,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    // Calculation Breakdown Metrics (Income, Expense, Net Balance)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Income
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total Income", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    totalIncome.toCurrencyString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        // Total Expenses
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total Expenses", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    totalExpense.toCurrencyString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }

                        // Net Balance
                        val netColor = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = netColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, netColor.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Net Balance", style = MaterialTheme.typography.labelSmall, color = netColor, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    netBalance.toCurrencyString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = netColor
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Explicit Category Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        TableCell(text = "Category", weight = 1.4f, isHeader = true)
                        TableCell(text = "Type", weight = 0.8f, isHeader = true)
                        TableCell(text = "Amount", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                    }

                    // Income Categories
                    incomeCategories.forEach { (catName, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(text = catName, weight = 1.4f, fontWeight = FontWeight.Medium)
                            TableCell(text = "Income", weight = 0.8f, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            TableCell(text = amount.toCurrencyString(), weight = 1.2f, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    }

                    // Expense Categories
                    expenseCategories.forEach { (catName, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(text = catName, weight = 1.4f, fontWeight = FontWeight.Medium)
                            TableCell(text = "Expense", weight = 0.8f, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            TableCell(text = amount.toCurrencyString(), weight = 1.2f, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    }

                    // Calculation Subtotal Formula Row at Bottom
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Monthly Calculation Subtotal",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Formula: Income (${totalIncome.toCurrencyString()}) - Expenses (${totalExpense.toCurrencyString()}) = Net Balance (${netBalance.toCurrencyString()})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
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
                                TableCell(text = summary.totalIncome.toCurrencyString(), weight = 1.1f, color = Color(0xFF10B981), textAlign = TextAlign.End)
                                TableCell(text = summary.totalExpense.toCurrencyString(), weight = 1.1f, color = Color(0xFFEF4444), textAlign = TextAlign.End)
                                TableCell(
                                    text = summary.netBalance.toCurrencyString(),
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
                            TableCell(text = grandTotalIncome.toCurrencyString(), weight = 1.1f, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            TableCell(text = grandTotalExpense.toCurrencyString(), weight = 1.1f, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            TableCell(
                                text = grandNetBalance.toCurrencyString(),
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

        // 2. Yearly Financial Breakdown (Calculation Breakdown Cards per Year)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Yearly Financial Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

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
                    groupedByYear.forEach { (yearStr, yearExpenses) ->
                        val incomeExpenses = yearExpenses.filter { it.category.isIncome }
                        val expenseExpenses = yearExpenses.filter { !it.category.isIncome }

                        val totalIncome = incomeExpenses.sumOf { it.expense.amount }
                        val totalExpense = expenseExpenses.sumOf { it.expense.amount }
                        val netBalance = totalIncome - totalExpense

                        val incomeCategories = incomeExpenses.groupBy { it.category.name }
                            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }
                            .toSortedMap()

                        val expenseCategories = expenseExpenses.groupBy { it.category.name }
                            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }
                            .toSortedMap()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column {
                                // Year Title Header
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Year $yearStr",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                // Top Metric Chips for Year (Total Income, Total Expenses, Net Balance)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Total Income
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("Total Income", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                totalIncome.toCurrencyString(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }

                                    // Total Expenses
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("Total Expenses", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                totalExpense.toCurrencyString(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }

                                    // Net Balance
                                    val netColor = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = netColor.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, netColor.copy(alpha = 0.3f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("Net Balance", style = MaterialTheme.typography.labelSmall, color = netColor, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                netBalance.toCurrencyString(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = netColor
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Explicit Category Table Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    TableCell(text = "Category", weight = 1.4f, isHeader = true)
                                    TableCell(text = "Type", weight = 0.8f, isHeader = true)
                                    TableCell(text = "Amount", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                                }

                                // Explicit Income Categories
                                incomeCategories.forEach { (catName, amount) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableCell(text = catName, weight = 1.4f, fontWeight = FontWeight.Medium)
                                        TableCell(text = "Income", weight = 0.8f, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                        TableCell(text = amount.toCurrencyString(), weight = 1.2f, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                    )
                                }

                                // Explicit Expense Categories
                                expenseCategories.forEach { (catName, amount) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableCell(text = catName, weight = 1.4f, fontWeight = FontWeight.Medium)
                                        TableCell(text = "Expense", weight = 0.8f, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                        TableCell(text = amount.toCurrencyString(), weight = 1.2f, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                    )
                                }

                                // Calculation Subtotal Formula Row at Bottom
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Yearly Calculation Subtotal",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Formula: Income (${totalIncome.toCurrencyString()}) - Expenses (${totalExpense.toCurrencyString()}) = Net Balance (${netBalance.toCurrencyString()})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
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
                                    TableCell(text = summary.totalIncome.toCurrencyString(), weight = 1.2f, color = Color(0xFF10B981), textAlign = TextAlign.End)
                                    TableCell(text = summary.totalExpense.toCurrencyString(), weight = 1.2f, color = Color(0xFFEF4444), textAlign = TextAlign.End)
                                    TableCell(
                                        text = summary.netBalance.toCurrencyString(),
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
                                TableCell(text = grandIncome.toCurrencyString(), weight = 1.2f, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                TableCell(text = grandExpense.toCurrencyString(), weight = 1.2f, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                TableCell(
                                    text = grandNet.toCurrencyString(),
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
                "Select Expense Table to Add Expense",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            if (childLekkas.isEmpty()) {
                Text("No expense tables found. Please create an expense table first.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose which expense table/event this expense belongs to:")
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
