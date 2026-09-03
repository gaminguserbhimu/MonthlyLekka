package com.vinay.monthlylekka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinay.monthlylekka.data.CategorySpec
import com.vinay.monthlylekka.data.Lekka
import com.vinay.monthlylekka.data.LekkaSummary
import com.vinay.monthlylekka.data.LekkaWithSummary
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(
    lekkas: List<LekkaWithSummary>,
    motherTableSummary: LekkaSummary? = null,
    onBack: () -> Unit,
    onTableClick: (Long) -> Unit,
    onSetDefaultLekka: (Long) -> Unit,
    onCreateLekka: (String, String?, String?, Boolean, List<CategorySpec>) -> Unit,
    onUpdateLekka: (Lekka, Boolean, List<CategorySpec>?) -> Unit,
    onDeleteLekka: (Lekka) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var lekkaToEdit by remember { mutableStateOf<Lekka?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Lekka?>(null) }

    val masterLekkaWithSummary = lekkas.find { it.lekka.isMotherTable } ?: lekkas.firstOrNull()
    val childLekkas = lekkas.filter { !it.lekka.isMotherTable }

    val masterSummary = motherTableSummary 
        ?: masterLekkaWithSummary?.summary 
        ?: LekkaSummary(0, 0.0, 0.0)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Expense Sheets",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Expense Sheet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section 1: Master Expense Sheet
            if (masterLekkaWithSummary != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 700.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "MASTER EXPENSE SHEET (AGGREGATED)",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        MasterTableOverviewCard(
                            masterItem = masterLekkaWithSummary,
                            summary = masterSummary,
                            onClick = { onTableClick(masterLekkaWithSummary.lekka.id) }
                        )
                    }
                }
            }

            // Section 2: Expense Sheets / Events
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 700.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expense Sheets / Events",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${childLekkas.size} Expense Sheets",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (childLekkas.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 700.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No expense sheets created yet. Click '+ Create Expense Sheet' to add one!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(childLekkas, key = { it.lekka.id }) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 700.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ChildTableCard(
                            item = item,
                            onClick = { onTableClick(item.lekka.id) },
                            onSetDefault = { onSetDefaultLekka(item.lekka.id) },
                            onEdit = { lekkaToEdit = item.lekka },
                            onDelete = { showDeleteConfirm = item.lekka }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        LekkaDialog(
            title = "Create Expense Sheet",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name: String, startDate: String?, endDate: String?, isDefault: Boolean, categories: List<CategorySpec> ->
                onCreateLekka(name, startDate, endDate, isDefault, categories)
                showCreateDialog = false
            }
        )
    }

    if (lekkaToEdit != null) {
        LekkaDialog(
            title = "Edit Expense Sheet",
            initialLekka = lekkaToEdit,
            onDismiss = { lekkaToEdit = null },
            onConfirm = { name: String, startDate: String?, endDate: String?, isDefault: Boolean, categories: List<CategorySpec> ->
                lekkaToEdit?.let {
                    onUpdateLekka(it.copy(name = name, startDate = startDate, endDate = endDate), isDefault, categories)
                }
                lekkaToEdit = null
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Expense Sheet") },
            text = { Text("Are you sure you want to delete '${showDeleteConfirm?.name}'? All associated transactions and categories will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm?.let { onDeleteLekka(it) }
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MasterTableOverviewCard(
    masterItem: LekkaWithSummary,
    summary: LekkaSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF064E3B), Color(0xFF0D9488))
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                        if (masterItem.lekka.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
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

                    Text(
                        text = "Tap to View Detail ➔",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Text(
                    text = masterItem.lekka.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Net Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.2f", summary.balance)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%,.2f", summary.totalIncome)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFA7F3D0)
                            )
                        }
                        Column {
                            Text(
                                text = "Outcome",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%,.2f", summary.totalExpense)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFECACA)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChildTableCard(
    item: LekkaWithSummary,
    onClick: () -> Unit,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val lekka = item.lekka
    val balance = item.summary?.balance ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lekka.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (lekka.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
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

                if (!lekka.startDate.isNullOrBlank() || !lekka.endDate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "📅 ${lekka.startDate ?: ""} - ${lekka.endDate ?: ""}".trim(' ', '-'),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Net Balance: ₹${String.format(Locale.getDefault(), "%,.2f", balance)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (balance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSetDefault) {
                    Icon(
                        imageVector = if (lekka.isDefault) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = if (lekka.isDefault) "Default Expense Sheet" else "Set as Default",
                        tint = if (lekka.isDefault) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit Expense Sheet",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Expense Sheet",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TablesScreenPreview() {
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

    MonthlyLekkaTheme {
        TablesScreen(
            lekkas = sampleLekkas,
            motherTableSummary = LekkaSummary(1, 70000.0, 23500.0),
            onBack = {},
            onTableClick = {},
            onSetDefaultLekka = {},
            onCreateLekka = { _, _, _, _, _ -> },
            onUpdateLekka = { _, _, _ -> },
            onDeleteLekka = {}
        )
    }
}
