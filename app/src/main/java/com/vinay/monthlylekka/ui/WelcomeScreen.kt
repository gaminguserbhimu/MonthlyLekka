package com.vinay.monthlylekka.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
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
import com.vinay.monthlylekka.data.LekkaSummary
import com.vinay.monthlylekka.data.LekkaWithSummary
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    lekkas: List<LekkaWithSummary>,
    selectedLekkaId: Long?,
    mostRecentTable: Lekka? = null,
    onLekkaSelected: (Long) -> Unit,
    onQuickAddClick: (Long) -> Unit,
    onSeeQuickNotesClick: (Long) -> Unit = {},
    onManageAllTablesClick: () -> Unit,
    modifier: Modifier = Modifier,
    motherTableSummary: LekkaSummary? = null,
    recentExpenses: List<ExpenseWithCategoryAndLekka> = emptyList(),
    onTableClick: (Long) -> Unit = onSeeQuickNotesClick,
    onHelpClick: () -> Unit = {}
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

    // Most Recently Updated Table Computation
    val targetMostRecentWithSummary = lekkas.find { it.lekka.id == mostRecentTable?.id }
        ?: childLekkas.find { it.lekka.isDefault }
        ?: childLekkas.firstOrNull()
    val targetMostRecentTable = targetMostRecentWithSummary?.lekka
    val targetMostRecentSummary = targetMostRecentWithSummary?.summary

    LaunchedEffect(lekkas, selectedLekkaId) {
        if (lekkas.isNotEmpty() && (selectedLekkaId == null || lekkas.none { it.lekka.id == selectedLekkaId })) {
            val defaultLekka = lekkas.find { it.lekka.isDefault } ?: lekkas.first()
            onLekkaSelected(defaultLekka.lekka.id)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isLargeScreen) 24.dp else 16.dp)
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                BrandedHeader(
                    isLargeScreen = isLargeScreen,
                    onHelpClick = onHelpClick
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (isLargeScreen) 24.dp else 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 2. Master Summary Card (Master Expense Table)
            val masterLekkaId = masterLekkaWithSummary?.lekka?.id
            OverallFinancialSummaryCard(
                summary = aggregatedSummary,
                onClick = {
                    masterLekkaId?.let { id ->
                        onLekkaSelected(id)
                        onTableClick(id)
                    }
                }
            )

            // 3. Most Recently Updated Expense Table Card (NO transaction rows)
            if (targetMostRecentTable != null) {
                MostRecentlyUpdatedExpenseTableCard(
                    table = targetMostRecentTable,
                    summary = targetMostRecentSummary,
                    isActive = (targetMostRecentTable.id == activeId),
                    onOpenTable = {
                        onLekkaSelected(targetMostRecentTable.id)
                        onTableClick(targetMostRecentTable.id)
                    }
                )
            }

            // 4. Active Expense Table Selector Dropdown
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
                        brush = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Active Expense Table: ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = activeLekkaWithSummary?.lekka?.name ?: "Select Expense Table",
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
                                        text = "👑 MASTER",
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
                            contentDescription = "Active Expense Table Selector Dropdown",
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

            // 5. Recent Tables Section
            val recentChildLekkas = childLekkas.take(2)
            if (recentChildLekkas.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 700.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recent Tables",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    recentChildLekkas.forEach { item ->
                        RecentTableCard(
                            lekkaWithSummary = item,
                            onClick = {
                                onLekkaSelected(item.lekka.id)
                                onTableClick(item.lekka.id)
                            }
                        )
                    }
                }
            }

            // 6. Primary Action Buttons ("Quick Add", "All Tables")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 700.dp),
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

                OutlinedButton(
                    onClick = onManageAllTablesClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TableChart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "All Tables",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 7. Recent Transactions Section (if any)
            if (recentExpenses.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 700.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    recentExpenses.take(5).forEach { item ->
                        ExpenseItemCard(
                            item = item,
                            isMotherTable = true,
                            onExpenseClick = { onTableClick(item.expense.lekkaId) }
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
fun RecentTableCard(
    lekkaWithSummary: LekkaWithSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val table = lekkaWithSummary.lekka
    val balance = lekkaWithSummary.summary?.balance ?: 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = table.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (table.isDefault) {
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Net Balance: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = balance.toCurrencyString(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (balance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Open Table",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BrandedHeader(
    isLargeScreen: Boolean = false,
    onHelpClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 700.dp)
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(if (isLargeScreen) 56.dp else 48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = "Monthly Expenses Logo",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(if (isLargeScreen) 32.dp else 26.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Monthly Expenses",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isLargeScreen) 28.sp else 22.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Smart Personal & Event Expense Manager",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        IconButton(onClick = onHelpClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                contentDescription = "Help & Guide",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun OverallFinancialSummaryCard(
    summary: LekkaSummary,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 700.dp)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Master Expense Table",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "👑 MASTER",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = summary.balance.toCurrencyString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        ),
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Income Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                        contentDescription = "Income",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
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
                                    text = summary.totalIncome.toCurrencyString(),
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
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                                        contentDescription = "Outcome",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
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
                                    text = summary.totalExpense.toCurrencyString(),
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
fun MostRecentlyUpdatedExpenseTableCard(
    table: Lekka,
    summary: LekkaSummary?,
    isActive: Boolean = false,
    onOpenTable: () -> Unit
) {
    val balance = summary?.balance ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 700.dp)
            .clickable { onOpenTable() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "⚡ MOST RECENTLY UPDATED EXPENSE TABLE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "View Details ➔",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = table.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
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
                        text = balance.toCurrencyString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (balance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
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
            Lekka(1, "Master Expense Table", isDefault = true, isMotherTable = true),
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
            mostRecentTable = sampleLekkas[1].lekka,
            motherTableSummary = LekkaSummary(1, 70000.0, 23500.0),
            recentExpenses = sampleExpenses,
            onLekkaSelected = {},
            onQuickAddClick = {},
            onSeeQuickNotesClick = {},
            onManageAllTablesClick = {},
            onTableClick = {}
        )
    }
}
