package com.vinay.monthlylekka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.Expense
import com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka
import com.vinay.monthlylekka.data.Lekka
import com.vinay.monthlylekka.data.LekkaSummary
import com.vinay.monthlylekka.data.LekkaWithSummary
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WelcomeScreen(
    lekkas: List<LekkaWithSummary>,
    selectedLekkaId: Long?,
    mostRecentSheet: Lekka? = null,
    onLekkaSelected: (Long) -> Unit,
    onQuickAddClick: (Long) -> Unit,
    onSeeQuickNotesClick: (Long) -> Unit,
    onManageAllTablesClick: () -> Unit,
    modifier: Modifier = Modifier,
    motherTableSummary: LekkaSummary? = null,
    recentExpenses: List<ExpenseWithCategoryAndLekka> = emptyList()
) {
    var showSelectChildDialogForQuickAdd by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp > 600

    val masterLekkaWithSummary = lekkas.find { it.lekka.isMotherTable } ?: lekkas.firstOrNull()
    val childLekkas = lekkas.filter { !it.lekka.isMotherTable }
    val activeLekkaWithSummary = lekkas.find { it.lekka.id == selectedLekkaId } ?: masterLekkaWithSummary
    val activeId = activeLekkaWithSummary?.lekka?.id

    val aggregatedSummary = motherTableSummary 
        ?: masterLekkaWithSummary?.summary 
        ?: LekkaSummary(0, 0.0, 0.0)

    // Most Recently Updated Sheet Computation
    val targetMostRecentWithSummary = lekkas.find { it.lekka.id == mostRecentSheet?.id }
        ?: childLekkas.find { it.lekka.isDefault }
        ?: childLekkas.firstOrNull()
    val targetMostRecentSheet = targetMostRecentWithSummary?.lekka
    val targetMostRecentSummary = targetMostRecentWithSummary?.summary

    // 2 Recent Transactions for Most Recently Updated Sheet
    val mostRecent2Expenses = remember(recentExpenses, targetMostRecentSheet) {
        if (targetMostRecentSheet != null) {
            recentExpenses.filter { it.expense.lekkaId == targetMostRecentSheet.id }.take(2)
        } else {
            emptyList()
        }
    }

    LaunchedEffect(lekkas, selectedLekkaId) {
        if (lekkas.isNotEmpty() && (selectedLekkaId == null || lekkas.none { it.lekka.id == selectedLekkaId })) {
            val defaultLekka = lekkas.find { it.lekka.isDefault } ?: lekkas.first()
            onLekkaSelected(defaultLekka.lekka.id)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (isLargeScreen) 32.dp else 16.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Branded Header
            item {
                BrandedHeader(isLargeScreen = isLargeScreen)
            }

            // 2. Master Summary Card (Overall Financial Summary)
            item {
                OverallFinancialSummaryCard(summary = aggregatedSummary)
            }

            // 3. Most Recently Updated Expense Sheet Card
            if (targetMostRecentSheet != null) {
                item {
                    MostRecentlyUpdatedExpenseSheetCard(
                        sheet = targetMostRecentSheet,
                        summary = targetMostRecentSummary,
                        recentTransactions = mostRecent2Expenses,
                        onOpenSheet = {
                            onLekkaSelected(targetMostRecentSheet.id)
                            onSeeQuickNotesClick(targetMostRecentSheet.id)
                        }
                    )
                }
            }

            // 4. Active Expense Sheet Selector Dropdown
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 700.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Active Expense Sheet: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = activeLekkaWithSummary?.lekka?.name ?: "Select Expense Sheet",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (activeLekkaWithSummary?.lekka?.isMotherTable == true) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "👑 MASTER EXPENSE SHEET",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (activeLekkaWithSummary?.lekka?.isDefault == true) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981)
                                    ) {
                                        Text(
                                            text = "★ DEFAULT",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "Active Expense Sheet Selector Dropdown",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.widthIn(min = 260.dp)
                    ) {
                        lekkas.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = item.lekka.name,
                                            fontWeight = if (item.lekka.id == activeId) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (item.lekka.isMotherTable) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = "👑 MASTER",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else if (item.lekka.isDefault) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF10B981)
                                            ) {
                                                Text(
                                                    text = "★ DEFAULT",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    onLekkaSelected(item.lekka.id)
                                }
                            )
                        }
                    }
                }
            }

            // 5. Active Expense Sheet Recent Records Preview
            item {
                val active2RecentExpenses = remember(recentExpenses, activeId) {
                    if (activeLekkaWithSummary?.lekka?.isMotherTable == true) {
                        recentExpenses.take(2)
                    } else {
                        recentExpenses.filter { it.expense.lekkaId == activeId }.take(2)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 700.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "RECENT RECORDS PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    if (active2RecentExpenses.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recent records found for this active expense sheet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        active2RecentExpenses.forEach { item ->
                            RecentTransactionPreviewRow(
                                item = item,
                                isMotherTable = activeLekkaWithSummary?.lekka?.isMotherTable == true
                            )
                        }
                    }
                }
            }

            // 6. Primary Action Buttons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 700.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (activeLekkaWithSummary?.lekka?.isMotherTable == true) {
                                    showSelectChildDialogForQuickAdd = true
                                } else {
                                    activeId?.let { onQuickAddClick(it) }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quick Add", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        FilledTonalButton(
                            onClick = {
                                activeId?.let { onSeeQuickNotesClick(it) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.List, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    OutlinedButton(
                        onClick = onManageAllTablesClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GridView,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Manage All Expense Sheets",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showSelectChildDialogForQuickAdd) {
        SelectChildTableDialogForWelcome(
            childLekkas = childLekkas.map { it.lekka },
            onDismiss = { showSelectChildDialogForQuickAdd = false },
            onChildTableSelected = { selectedChildId: Long ->
                showSelectChildDialogForQuickAdd = false
                onQuickAddClick(selectedChildId)
            }
        )
    }
}

@Composable
fun BrandedHeader(isLargeScreen: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 700.dp)
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(if (isLargeScreen) 64.dp else 52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = "Monthly Expenses Logo",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(if (isLargeScreen) 36.dp else 28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Monthly Expenses",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isLargeScreen) 32.sp else 24.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Smart Personal & Event Expense Manager",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun OverallFinancialSummaryCard(summary: LekkaSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 700.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "OVERALL FINANCIAL SUMMARY (MASTER EXPENSE SHEET)",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )

                Column {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.2f", summary.balance)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Income Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                        contentDescription = "Income",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Total Income",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%,.2f", summary.totalIncome)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Total Outcome/Expense Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                                        contentDescription = "Outcome",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Total Outcome",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%,.2f", summary.totalExpense)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
fun MostRecentlyUpdatedExpenseSheetCard(
    sheet: Lekka,
    summary: LekkaSummary?,
    recentTransactions: List<ExpenseWithCategoryAndLekka>,
    onOpenSheet: () -> Unit
) {
    val balance = summary?.balance ?: 0.0
    val dateRangeText = remember(sheet.startDate, sheet.endDate) {
        if (!sheet.startDate.isNullOrBlank() || !sheet.endDate.isNullOrBlank()) {
            "📅 ${sheet.startDate ?: ""} - ${sheet.endDate ?: ""}".trim(' ', '-')
        } else {
            "📅 No date range specified"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 700.dp)
            .clickable { onOpenSheet() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "⚡ MOST RECENTLY UPDATED EXPENSE SHEET",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "View Details ➔",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = sheet.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateRangeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.2f", balance)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (balance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            Text(
                text = "RECENT TRANSACTIONS PREVIEW (2)",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            if (recentTransactions.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent transactions in this sheet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                recentTransactions.forEach { item ->
                    RecentTransactionPreviewRow(
                        item = item,
                        isMotherTable = false
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTransactionPreviewRow(
    item: ExpenseWithCategoryAndLekka,
    isMotherTable: Boolean
) {
    val expense = item.expense
    val category = item.category

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Date Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = expense.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                // Category Avatar & Name Badge
                CategoryIconAvatar(
                    categoryName = category.name,
                    colorHex = category.colorHex,
                    isIncome = category.isIncome,
                    size = 32.dp,
                    iconSize = 18.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (isMotherTable && item.lekkaName.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "[${item.lekkaName}]",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = expense.description.ifBlank { "No description" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${if (category.isIncome) "+" else "-"} ₹${String.format(Locale.getDefault(), "%,.2f", expense.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = if (category.isIncome) Color(0xFF2E7D32) else Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
fun SelectChildTableDialogForWelcome(
    childLekkas: List<Lekka>,
    onDismiss: () -> Unit,
    onChildTableSelected: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Expense Sheet", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Direct entry into Master Expense Sheet is read-only. Please select an Expense Sheet to record this transaction:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (childLekkas.isEmpty()) {
                    Text(
                        "No Expense Sheets available. Please create an Expense Sheet first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        items(childLekkas.size) { index ->
                            val lekka = childLekkas[index]
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
fun WelcomeScreenPreview() {
    val sampleLekkas = listOf(
        LekkaWithSummary(
            Lekka(1, "Master Expense Sheet", isDefault = true, isMotherTable = true),
            LekkaSummary(1, 70000.0, 23500.0)
        ),
        LekkaWithSummary(
            Lekka(2, "Home Expenses", "01/09/2026", "30/09/2026", isDefault = false, isMotherTable = false),
            LekkaSummary(2, 50000.0, 15000.0)
        ),
        LekkaWithSummary(
            Lekka(3, "Goa Trip", "05/09/2026", "10/09/2026", isDefault = false, isMotherTable = false),
            LekkaSummary(3, 20000.0, 8500.0)
        )
    )

    val sampleExpenses = listOf(
        ExpenseWithCategoryAndLekka(
            Expense(1, 2, "Hotel Booking", 5000.0, 1, LocalDate.now()),
            Category(1, 2, "Travel", "#1E88E5", false),
            "Goa Trip"
        ),
        ExpenseWithCategoryAndLekka(
            Expense(2, 2, "Salary", 50000.0, 2, LocalDate.now()),
            Category(2, 2, "Income", "#2E7D32", true),
            "Home Expenses"
        )
    )

    MonthlyLekkaTheme {
        WelcomeScreen(
            lekkas = sampleLekkas,
            selectedLekkaId = 1,
            mostRecentSheet = sampleLekkas[1].lekka,
            motherTableSummary = LekkaSummary(1, 70000.0, 23500.0),
            recentExpenses = sampleExpenses,
            onLekkaSelected = {},
            onQuickAddClick = {},
            onSeeQuickNotesClick = {},
            onManageAllTablesClick = {}
        )
    }
}
