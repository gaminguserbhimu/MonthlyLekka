package com.vinay.monthlylekka.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onExportCsv: (Uri) -> Unit = {},
    onExportBackup: (Uri) -> Unit = {},
    onImportBackup: (Uri) -> Unit = {}
) {
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { onExportCsv(it) }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { onExportBackup(it) }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportBackup(it) }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Help & Guide",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Intro Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Monthly Expenses User Guide",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Learn how to effortlessly organize, analyze, and manage your finances.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 1: 👑 Master Expense Table
            HelpSectionCard(
                icon = Icons.Rounded.Stars,
                iconTint = Color(0xFFFFB300),
                title = "👑 Master Expense Table",
                badgeText = "MASTER",
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                HelpBulletItem(
                    title = "Central Aggregator",
                    description = "Combines expenses and income from all child tables automatically into an overall summary."
                )
                HelpBulletItem(
                    title = "Complete Financial Snapshot",
                    description = "Gives you a real-time total view of your overall balance, total income, and total expenses across all personal and event tables."
                )
                HelpBulletItem(
                    title = "Read-Only Overview",
                    description = "All entries are automatically aggregated from individual child tables, keeping your overall balances accurate without duplicate entries."
                )
            }

            // Section 2: 📊 Expense Tables & Events
            HelpSectionCard(
                icon = Icons.Rounded.TableChart,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "📊 Expense Tables & Events",
                badgeText = "TABLES",
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                HelpBulletItem(
                    title = "Create Custom Tables",
                    description = "Set up dedicated tables for individual events or budgets (e.g. Home Expenses, Goa Trip, College Fees, Wedding)."
                )
                HelpBulletItem(
                    title = "Switching & Management",
                    description = "Easily switch between tables from the Home Screen or Manage All Tables menu."
                )
                HelpBulletItem(
                    title = "Default & Event Dates",
                    description = "Mark a default table for fast logging, and specify start/end dates for event-specific expense tracking."
                )
            }

            // Section 3: 💡 Category Rules & Customization
            HelpSectionCard(
                icon = Icons.Rounded.Category,
                iconTint = Color(0xFF10B981),
                title = "💡 Category Rules & Customization",
                badgeText = "CATEGORIES",
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                HelpBulletItem(
                    title = "Income Rules",
                    description = "\"Income\" is strictly locked at the #1 position and is non-deletable. Use it for money received, earnings, or refunds with your custom description."
                )
                HelpBulletItem(
                    title = "Custom Categories",
                    description = "All new custom categories you create are saved as Expense type to organize your spendings (e.g. Groceries, Shopping, Rent)."
                )
            }

            // Section 4: 📈 4-Slide Table Detail View
            HelpSectionCard(
                icon = Icons.Rounded.Swipe,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = "📈 4-Slide Table Detail View",
                badgeText = "SLIDES",
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "Swipe horizontally in any Table Detail screen to explore 4 interactive views:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                HelpSlideSubCard(
                    slideNumber = "1",
                    slideTitle = "Transactions & Multi-Select Batch Delete",
                    icon = Icons.Rounded.CheckCircle,
                    description = "View all logged transactions chronologically. Long-press any transaction row to enter multi-select mode, tap items to select, and perform bulk batch deletion."
                )

                Spacer(modifier = Modifier.height(8.dp))

                HelpSlideSubCard(
                    slideNumber = "2",
                    slideTitle = "Interactive Category Pie Charts",
                    icon = Icons.Rounded.PieChart,
                    description = "Visual representation of expense distribution. Tap on any pie slice to inspect the exact amount and percentage breakdown for that category."
                )

                Spacer(modifier = Modifier.height(8.dp))

                HelpSlideSubCard(
                    slideNumber = "3",
                    slideTitle = "Monthly Breakdown Table",
                    icon = Icons.Rounded.Analytics,
                    description = "Monthly summary calculation table.",
                    formulaText = "Formula: Income (₹ XX) - Expenses (₹ XX) = Net Balance (₹ XX)"
                )

                Spacer(modifier = Modifier.height(8.dp))

                HelpSlideSubCard(
                    slideNumber = "4",
                    slideTitle = "Yearly Breakdown Table",
                    icon = Icons.Rounded.TableChart,
                    description = "Yearly summary calculation table tracking long-term annual financial balances and spending trends."
                )
            }

            // Section 5: ⚡ Quick Add & Usage-Based Sorting
            HelpSectionCard(
                icon = Icons.Rounded.Bolt,
                iconTint = Color(0xFFFF9800),
                title = "⚡ Quick Add & Usage-Based Sorting",
                badgeText = "QUICK ADD",
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                HelpBulletItem(
                    title = "Fast Expense Logging",
                    description = "Log transactions instantly with Quick Add from the home screen or inside any active table."
                )
                HelpBulletItem(
                    title = "Smart Usage-Based Category Sorting",
                    description = "Your most frequently used categories automatically bubble up to the top of the category dropdown right below Income, minimizing taps during logging."
                )
            }

            // Section 6: 💾 Data Backup & Export
            DataBackupSection(
                onExportCsvClick = { exportCsvLauncher.launch("monthly_lekka_expenses.csv") },
                onExportBackupClick = { exportJsonLauncher.launch("monthly_lekka_backup.json") },
                onImportBackupClick = { importJsonLauncher.launch("application/json") }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpSectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    badgeText: String,
    containerColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = iconTint.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = iconTint.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun HelpBulletItem(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HelpSlideSubCard(
    slideNumber: String,
    slideTitle: String,
    icon: ImageVector,
    description: String,
    formulaText: String? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = slideNumber,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = slideTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (formulaText != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = formulaText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HelpScreenPreview() {
    MonthlyLekkaTheme {
        HelpScreen(onBack = {})
    }
}
