package com.vinay.monthlylekka.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinay.monthlylekka.data.CategorySpec
import com.vinay.monthlylekka.data.DEFAULT_CATEGORY_SPECS
import com.vinay.monthlylekka.data.Lekka

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LekkaDialog(
    title: String = "Create Expense Table",
    initialLekka: Lekka? = null,
    initialCategories: List<CategorySpec>? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, isDefault: Boolean, categories: List<CategorySpec>) -> Unit
) {
    var name by remember { mutableStateOf(initialLekka?.name ?: "") }
    var isDefault by remember { mutableStateOf(initialLekka?.isDefault ?: false) }

    var availableCategories by remember {
        mutableStateOf(initialCategories ?: DEFAULT_CATEGORY_SPECS)
    }
    var selectedCategories by remember {
        mutableStateOf((initialCategories ?: DEFAULT_CATEGORY_SPECS).toSet())
    }

    var customCategoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Expense Table Name *") },
                    placeholder = { Text("e.g. Home Expenses, Goa Trip") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDefault = !isDefault }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set as Default Expense Table", style = MaterialTheme.typography.bodyMedium)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Category Selection & Customization Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Categories Customization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Select default categories or add custom expense categories for this table:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Prominent Instruction Note for Income
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 Note: Use 'Income' for money received. You can enter your own description for each income record.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Default & Added Categories List with Checkboxes
                    availableCategories.forEach { categorySpec ->
                        val isIncomeSpec = categorySpec.isIncome || categorySpec.name.equals("Income", ignoreCase = true)
                        val isChecked = selectedCategories.contains(categorySpec) || isIncomeSpec

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isIncomeSpec) {
                                    selectedCategories = if (isChecked) {
                                        selectedCategories - categorySpec
                                    } else {
                                        selectedCategories + categorySpec
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                enabled = !isIncomeSpec,
                                onCheckedChange = { checked ->
                                    if (!isIncomeSpec) {
                                        selectedCategories = if (checked) {
                                            selectedCategories + categorySpec
                                        } else {
                                            selectedCategories - categorySpec
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            CategoryIconAvatar(
                                categoryName = categorySpec.name,
                                colorHex = categorySpec.colorHex,
                                isIncome = categorySpec.isIncome,
                                size = 28.dp,
                                iconSize = 16.dp
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = categorySpec.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.weight(1f)
                            )

                            if (categorySpec.isIncome) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981)
                                ) {
                                    Text(
                                        text = "INCOME",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (!categorySpec.isDefault) {
                                IconButton(
                                    onClick = {
                                        availableCategories = availableCategories - categorySpec
                                        selectedCategories = selectedCategories - categorySpec
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Remove custom category",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add Custom Category Field
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customCategoryName,
                            onValueChange = { customCategoryName = it },
                            label = { Text("+ Add Custom Category") },
                            placeholder = { Text("e.g. Shopping, Fuel") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val trimmed = customCategoryName.trim()
                                if (trimmed.isNotBlank() && availableCategories.none { it.name.equals(trimmed, ignoreCase = true) }) {
                                    // Custom categories are strictly created as Expense categories (isIncome = false)
                                    val newSpec = CategorySpec(
                                        name = trimmed,
                                        colorHex = "#00ACC1",
                                        isIncome = false,
                                        isDefault = false
                                    )
                                    availableCategories = availableCategories + newSpec
                                    selectedCategories = selectedCategories + newSpec
                                    customCategoryName = ""
                                }
                            },
                            enabled = customCategoryName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            isDefault,
                            selectedCategories.toList()
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
