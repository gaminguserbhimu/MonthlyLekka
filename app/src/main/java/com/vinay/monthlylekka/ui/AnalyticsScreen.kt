package com.vinay.monthlylekka.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.Expense
import com.vinay.monthlylekka.data.ExpenseWithCategory
import com.vinay.monthlylekka.data.MonthlySummary
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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
    val expenseColor = Color(0xFFF06292) // Pink (as requested)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
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
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
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
                    TableCell(text = "₹${"%.0f".format(summary.totalIncome)}", weight = 1.2f, color = Color(0xFF2E7D32), textAlign = TextAlign.End)
                    TableCell(text = "₹${"%.0f".format(summary.totalExpense)}", weight = 1.2f, color = Color(0xFFC62828), textAlign = TextAlign.End)
                    TableCell(
                        text = "₹${"%.0f".format(summary.netBalance)}",
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
    val expenseData = expenses
        .filter { !it.category.isIncome }
        .groupBy { it.category.name }
        .mapValues { it.value.sumOf { exp -> exp.expense.amount } }
    
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
    val categoryColors = expenses.associateBy({ it.category.name }, { Color(it.category.colorHex.toColorInt()) })

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Expenses by Category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    expenseData.forEach { (name, amount) ->
                        val sweepAngle = (amount / total).toFloat() * 360f
                        drawArc(
                            color = categoryColors[name] ?: Color.Gray,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true
                        )
                        startAngle += sweepAngle
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Legend
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                expenseData.forEach { (name, _) ->
                    LegendItem(name, categoryColors[name] ?: Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
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
                            Text("₹${"%.0f".format(total)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
