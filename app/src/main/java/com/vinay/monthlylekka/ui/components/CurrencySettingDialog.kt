package com.vinay.monthlylekka.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinay.monthlylekka.ui.theme.MonthlyLekkaTheme

@Composable
fun CurrencySettingDialog(
    currentSymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val popularSymbols = listOf("₹", "$", "€", "£", "¥")
    var selectedSymbol by remember { mutableStateOf(currentSymbol) }
    var isCustom by remember { mutableStateOf(currentSymbol !in popularSymbols) }
    var customSymbolText by remember { mutableStateOf(if (currentSymbol !in popularSymbols) currentSymbol else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "💱 Currency Symbol",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select a currency symbol or enter a custom symbol for displaying amounts:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    popularSymbols.forEach { symbol ->
                        val isSelected = (!isCustom && selectedSymbol == symbol)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isCustom = false
                                selectedSymbol = symbol
                            },
                            label = {
                                Text(
                                    text = symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = customSymbolText,
                    onValueChange = { text ->
                        customSymbolText = text
                        isCustom = true
                    },
                    label = { Text("Custom Symbol (e.g. AED, A$, ₱, ₩)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalSymbol = if (isCustom) {
                        customSymbolText.trim().ifEmpty { currentSymbol }
                    } else {
                        selectedSymbol
                    }
                    onConfirm(finalSymbol)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CurrencySettingDialogPreview() {
    MonthlyLekkaTheme {
        CurrencySettingDialog(
            currentSymbol = "₹",
            onDismiss = {},
            onConfirm = {}
        )
    }
}
