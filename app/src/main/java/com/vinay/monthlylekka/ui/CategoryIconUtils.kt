package com.vinay.monthlylekka.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

fun getCategoryIcon(categoryName: String, isIncome: Boolean = false): ImageVector {
    return when (categoryName.lowercase().trim()) {
        "kirani" -> Icons.Rounded.ShoppingCart
        "kaipalle" -> Icons.Rounded.ShoppingBag
        "food" -> Icons.Rounded.Restaurant
        "bills" -> Icons.AutoMirrored.Rounded.ReceiptLong
        "travel" -> Icons.Rounded.DirectionsCar
        "hospital" -> Icons.Rounded.LocalHospital
        "income" -> Icons.Rounded.Payments
        else -> if (isIncome) Icons.Rounded.Payments else Icons.Rounded.Category
    }
}

@Composable
fun CategoryIconAvatar(
    categoryName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    isIncome: Boolean = false,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp
) {
    val categoryColor = try {
        Color(colorHex.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val icon = getCategoryIcon(categoryName, isIncome)

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = categoryColor.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = categoryName,
                tint = categoryColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
