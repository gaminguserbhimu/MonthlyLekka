package com.vinay.monthlylekka.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.vinay.monthlylekka.data.ExpenseWithCategoryAndLekka
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseItemCard(
    item: ExpenseWithCategoryAndLekka,
    modifier: Modifier = Modifier,
    isMotherTable: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongPressSelect: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") onDelete: (() -> Unit)? = null,
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
        modifier = modifier
            .fillMaxWidth()
            .then(
                if ((onClick != null) || isSelectionMode) {
                    Modifier.combinedClickable(
                        onClick = {
                            if (isSelectionMode) {
                                onToggleSelect()
                            } else {
                                onClick?.invoke()
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                onLongPressSelect()
                            } else {
                                onToggleSelect()
                            }
                        }
                    )
                } else Modifier
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

                // Origin Tag Pill (if Master view)
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

                val amountText = "${if (category.isIncome) "+" else "-"} ${expense.amount.toCurrencyString()}"
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

@Suppress("Unused")
@Composable
fun ExpenseItem(
    item: ExpenseWithCategoryAndLekka,
    modifier: Modifier = Modifier,
    isMotherTable: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongPressSelect: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    ExpenseItemCard(
        item = item,
        modifier = modifier,
        isMotherTable = isMotherTable,
        isSelectionMode = isSelectionMode,
        isSelected = isSelected,
        onToggleSelect = onToggleSelect,
        onLongPressSelect = onLongPressSelect,
        onClick = onClick,
        onDelete = onDelete
    )
}
