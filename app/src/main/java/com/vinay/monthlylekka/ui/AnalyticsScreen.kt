package com.vinay.monthlylekka.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.graphics.toColorInt
import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.Expense
import com.vinay.monthlylekka.data.ExpenseWithCategory
import com.vinay.monthlylekka.data.MonthlySummary
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    summaries: List<MonthlySummary>,
    expenses: List<ExpenseWithCategory>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Expense Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Income vs Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                BarChart(summaries)
            }

            item {
                PieChart(expenses)
            }

            item {
                CategoryBreakdownTable(expenses)
            }

            item {
                Text(
                    text = "Monthly Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                SummaryTable(summaries)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun BarChart(summaries: List<MonthlySummary>) {
    if (summaries.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data available for chart", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    // Take last 6 months for the chart
    val chartData = summaries.sortedBy { it.month }.takeLast(6)
    val maxVal = chartData.maxOf { maxOf(it.totalIncome, it.totalExpense) }.coerceAtLeast(1.0)
    
    val incomeColor = Color(0xFF4CAF50) // Green
    val expenseColor = Color(0xFFF06292) // Pink

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val chartRadius = (min(maxWidth, maxHeight) * 0.38f)
                val chartHeight = (chartRadius * 2.5f).coerceIn(160.dp, 240.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    chartData.forEach { summary ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Income Bar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight((summary.totalIncome / maxVal).toFloat().coerceIn(0.01f, 1f))
                                        .background(incomeColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                                // Expense Bar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight((summary.totalExpense / maxVal).toFloat().coerceIn(0.01f, 1f))
                                        .background(expenseColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val monthLabel = try {
                                YearMonth.parse(summary.month).format(DateTimeFormatter.ofPattern("MMM"))
                            } catch (_: Exception) {
                                summary.month
                            }
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem("Income", incomeColor)
                Spacer(modifier = Modifier.width(24.dp))
                LegendItem("Expense", expenseColor)
            }
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    color: Color,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 14.dp else 12.dp)
                .background(color, RoundedCornerShape(if (isSelected) 4.dp else 2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SummaryTable(summaries: List<MonthlySummary>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                TableCell(text = "Month", weight = 1f, isHeader = true)
                TableCell(text = "Income", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                TableCell(text = "Expense", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
                TableCell(text = "Net", weight = 1.2f, isHeader = true, textAlign = TextAlign.End)
            }

            summaries.forEach { summary ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedMonth = try {
                        YearMonth.parse(summary.month).format(DateTimeFormatter.ofPattern("MMM yyyy"))
                    } catch (e: Exception) {
                        summary.month
                    }
                    TableCell(text = formattedMonth, weight = 1f)
                    TableCell(text = summary.totalIncome.toCurrencyString(), weight = 1.2f, color = Color(0xFF2E7D32), textAlign = TextAlign.End)
                    TableCell(text = summary.totalExpense.toCurrencyString(), weight = 1.2f, color = Color(0xFFC62828), textAlign = TextAlign.End)
                    TableCell(
                        text = summary.netBalance.toCurrencyString(),
                        weight = 1.2f,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = if (summary.netBalance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = if (isHeader) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight ?: if (isHeader) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else color,
        textAlign = textAlign
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PieChart(expenses: List<ExpenseWithCategory>) {
    val expenseData = remember(expenses) {
        expenses
            .filter { !it.category.isIncome }
            .groupBy { it.category.name }
            .mapValues { it.value.sumOf { exp -> exp.expense.amount } }
    }
    
    if (expenseData.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No expense data for pie chart", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    val total = expenseData.values.sum()
    val categoryColors = remember(expenses) {
        expenses.associateBy({ it.category.name }, { Color(it.category.colorHex.toColorInt()) })
    }

    var selectedCategoryName by remember(expenseData) {
        mutableStateOf<String?>(expenseData.keys.firstOrNull())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Expenses by Category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                val chartRadius = (min(maxWidth, maxHeight) * 0.38f)
                val chartDiameter = chartRadius * 2
                Box(modifier = Modifier.size(chartDiameter), contentAlignment = Alignment.Center) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(expenseData, total) {
                                detectTapGestures { tapOffset ->
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val dx = tapOffset.x - centerX
                                    val dy = tapOffset.y - centerY
                                    val distance = sqrt(dx * dx + dy * dy)
                                    val maxRadius = min(size.width, size.height) / 2f

                                    if (distance <= maxRadius && total > 0) {
                                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        if (angle < 0) angle += 360f
                                        val touchAngle = (angle - 270f + 360f) % 360f

                                        var currentSweep = 0f
                                        for ((catName, amount) in expenseData) {
                                            val sweep = ((amount / total) * 360f).toFloat()
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
                        expenseData.forEach { (name, amount) ->
                            val sweepAngle = ((amount / total) * 360f).toFloat()
                            val isSelected = (name == selectedCategoryName)
                            val color = categoryColors[name] ?: Color.Gray

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
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Legend with tap selection
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                expenseData.forEach { (name, _) ->
                    LegendItem(
                        label = name,
                        color = categoryColors[name] ?: Color.Gray,
                        isSelected = (name == selectedCategoryName),
                        onClick = { selectedCategoryName = name }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            // Interactive Category Details Card
            if (selectedCategoryName != null) {
                val selAmount = expenseData[selectedCategoryName] ?: 0.0
                val percent = if (total > 0) (selAmount / total * 100) else 0.0
                val selColor = categoryColors[selectedCategoryName] ?: Color.Gray

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
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

@Composable
fun CategoryBreakdownTable(expenses: List<ExpenseWithCategory>) {
    val groupedByMonth = expenses
        .filter { !it.category.isIncome }
        .groupBy { it.expense.date.format(DateTimeFormatter.ofPattern("MMM yyyy")) }
        .mapValues { (_, monthExpenses) ->
            monthExpenses.groupBy { it.category.name }
                .mapValues { it.value.sumOf { exp -> exp.expense.amount } }
        }

    if (groupedByMonth.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Category Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        groupedByMonth.forEach { (month, categoryTotals) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(month, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("Category", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("Total Cost", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    }

                    categoryTotals.forEach { (category, total) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, style = MaterialTheme.typography.bodyMedium)
                            Text(total.toCurrencyString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun AnalyticsPreview() {
    val dummySummaries = listOf(
        MonthlySummary("2026-08", 55000.0, 42000.0),
        MonthlySummary("2026-09", 58000.0, 45000.0)
    )
    
    val catFood = Category(id = 1, lekkaId = 1, name = "Food", colorHex = "#F44336", isIncome = false)
    val catTransport = Category(id = 2, lekkaId = 1, name = "Transport", colorHex = "#2196F3", isIncome = false)
    val catIncome = Category(id = 3, lekkaId = 1, name = "Salary", colorHex = "#4CAF50", isIncome = true)
    
    val dummyExpenses = listOf(
        ExpenseWithCategory(Expense(id = 1, lekkaId = 1, description = "Groceries", amount = 2000.0, categoryId = 1, date = LocalDate.now()), catFood),
        ExpenseWithCategory(Expense(id = 2, lekkaId = 1, description = "Fuel", amount = 3000.0, categoryId = 2, date = LocalDate.now()), catTransport),
        ExpenseWithCategory(Expense(id = 3, lekkaId = 1, description = "Salary", amount = 50000.0, categoryId = 3, date = LocalDate.now()), catIncome)
    )

    MonthlyLekkaTheme {
        AnalyticsScreen(
            summaries = dummySummaries,
            expenses = dummyExpenses,
            onBack = {}
        )
    }
}
