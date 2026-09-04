package com.vinay.monthlylekka.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinay.monthlylekka.data.Category
import com.vinay.monthlylekka.data.DEFAULT_CATEGORY_SPECS
import com.vinay.monthlylekka.data.ExpenseWithCategory
import com.vinay.monthlylekka.data.Lekka
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    categories: List<Category>,
    availableLekkas: List<Lekka> = emptyList(),
    initialLekkaId: Long? = null,
    expenseToEdit: ExpenseWithCategory? = null,
    onTableSelected: (Long) -> Unit = {},
    onSave: (Long?, String, Double, Long, LocalDate, Long?) -> Unit,
    onDelete: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
) {
    var description by remember(expenseToEdit) { mutableStateOf(expenseToEdit?.expense?.description ?: "") }
    var amount by remember(expenseToEdit) { mutableStateOf(expenseToEdit?.expense?.amount?.toString() ?: "") }
    
    var selectedLekka by remember(availableLekkas, initialLekkaId, expenseToEdit) {
        mutableStateOf<Lekka?>(
            availableLekkas.find { it.id == expenseToEdit?.expense?.lekkaId }
                ?: availableLekkas.find { it.id == initialLekkaId }
                ?: availableLekkas.find { it.isDefault }
                ?: availableLekkas.firstOrNull()
        )
    }

    LaunchedEffect(selectedLekka?.id) {
        selectedLekka?.id?.let { lekkaId ->
            onTableSelected(lekkaId)
        }
    }

    val effectiveCategories = remember(categories, selectedLekka) {
        if (categories.isNotEmpty()) {
            categories
        } else {
            val targetLekkaId = selectedLekka?.id ?: 0L
            DEFAULT_CATEGORY_SPECS.mapIndexed { index, spec ->
                Category(
                    id = (index + 1).toLong(),
                    lekkaId = targetLekkaId,
                    name = spec.name,
                    colorHex = spec.colorHex,
                    isIncome = spec.isIncome
                )
            }
        }
    }

    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(effectiveCategories, selectedLekka?.id, expenseToEdit) {
        if (effectiveCategories.isNotEmpty()) {
            val currentName = selectedCategory?.name
            val matchInNewCategories = effectiveCategories.find { it.name == currentName }
            val editMatch = if (expenseToEdit?.expense?.lekkaId == selectedLekka?.id && expenseToEdit != null) {
                effectiveCategories.find { it.id == expenseToEdit.category.id || it.name == expenseToEdit.category.name }
            } else null

            selectedCategory = matchInNewCategories
                ?: editMatch
                ?: effectiveCategories.find { it.name == "Food" }
                ?: effectiveCategories.firstOrNull()
        }
    }

    var date by remember(expenseToEdit) { mutableStateOf(expenseToEdit?.expense?.date ?: LocalDate.now()) }
    var expanded by remember { mutableStateOf(false) }
    var tableDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (expenseToEdit == null) "Add Expense" else "Edit Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (expenseToEdit != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Expense",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val categoryId = selectedCategory?.id
                    val targetLekkaId = selectedLekka?.id
                    if (description.isNotBlank() && amountValue > 0 && categoryId != null) {
                        onSave(expenseToEdit?.expense?.id, description, amountValue, categoryId, date, targetLekkaId)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(if (expenseToEdit == null) Icons.Default.Check else Icons.Default.Check, contentDescription = "Save Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Amount Input (Large)
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                label = { Text("Amount") },
                placeholder = { Text("0") },
                prefix = { Text("₹ ", style = MaterialTheme.typography.headlineSmall) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("What was this for?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Date Selection
            OutlinedTextField(
                value = date.format(dateFormatter),
                onValueChange = { },
                label = { Text("Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                    }
                },
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect { interaction ->
                                if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                    showDatePicker = true
                                }
                            }
                        }
                    }
            )

            // Category Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            selectedCategory?.let { category ->
                                CategoryIconAvatar(
                                    categoryName = category.name,
                                    colorHex = category.colorHex,
                                    isIncome = category.isIncome,
                                    size = 28.dp,
                                    iconSize = 16.dp
                                )
                            }
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        effectiveCategories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryIconAvatar(
                                            categoryName = category.name,
                                            colorHex = category.colorHex,
                                            isIncome = category.isIncome,
                                            size = 28.dp,
                                            iconSize = 16.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Expense Table Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Expense Table",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                ExposedDropdownMenuBox(
                    expanded = tableDropdownExpanded,
                    onExpandedChange = { tableDropdownExpanded = !tableDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLekka?.name ?: "Select Expense Table",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tableDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.TableChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = tableDropdownExpanded,
                        onDismissRequest = { tableDropdownExpanded = false }
                    ) {
                        availableLekkas.forEach { lekka ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.TableChart,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(lekka.name)
                                    }
                                },
                                onClick = {
                                    selectedLekka = lekka
                                    tableDropdownExpanded = false
                                    onTableSelected(lekka.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddExpensePreview() {
    val dummyCategories = listOf(
        Category(id = 1, lekkaId = 1, name = "Income", colorHex = "#2E7D32", isIncome = true),
        Category(id = 2, lekkaId = 1, name = "Food", colorHex = "#E53935", isIncome = false)
    )
    val dummyLekkas = listOf(
        Lekka(id = 1, name = "Monthly Expenses", isDefault = true),
        Lekka(id = 2, name = "Goa Trip", isDefault = false)
    )
    MonthlyLekkaTheme {
        AddExpenseScreen(
            categories = dummyCategories,
            availableLekkas = dummyLekkas,
            onSave = { _, _, _, _, _, _ -> },
            onBack = {}
        )
    }
}
