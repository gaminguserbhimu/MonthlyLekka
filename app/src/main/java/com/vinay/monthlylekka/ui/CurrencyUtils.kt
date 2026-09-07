package com.vinay.monthlylekka.ui

import kotlin.math.abs
import kotlin.math.round

object CurrencyUtils {
    var activeCurrencySymbol: String = "₹"
}

/**
 * Standardized Currency Formatter
 * Formats numbers with comma grouping and active currency symbol prefix.
 * Examples (with symbol '₹'):
 *   150000.0 -> "₹ 1,50,000"
 *   5000.0   -> "₹ 5,000"
 *   0.0      -> "₹ 0"
 *  -5000.0   -> "-₹ 5,000"
 */
fun Double.toCurrencyString(symbol: String = CurrencyUtils.activeCurrencySymbol): String {
    val isNegative = this < 0
    val absVal = abs(this)
    val longPart = absVal.toLong()
    val decimalPart = round((absVal - longPart) * 100).toInt()

    val strPart = longPart.toString()
    val formattedInt = if (strPart.length <= 3) {
        strPart
    } else {
        val last3 = strPart.takeLast(3)
        val rest = strPart.dropLast(3)
        val restGrouped = rest.reversed().chunked(2).joinToString(",").reversed()
        "$restGrouped,$last3"
    }

    val formattedNum = if (decimalPart > 0) {
        val decStr = if (decimalPart < 10) "0$decimalPart" else "$decimalPart"
        "$formattedInt.$decStr"
    } else {
        formattedInt
    }

    val sign = if (isNegative) "-" else ""
    return "$sign$symbol $formattedNum"
}

fun Float.toCurrencyString(symbol: String = CurrencyUtils.activeCurrencySymbol): String = this.toDouble().toCurrencyString(symbol)
fun Int.toCurrencyString(symbol: String = CurrencyUtils.activeCurrencySymbol): String = this.toDouble().toCurrencyString(symbol)
fun Long.toCurrencyString(symbol: String = CurrencyUtils.activeCurrencySymbol): String = this.toDouble().toCurrencyString(symbol)
