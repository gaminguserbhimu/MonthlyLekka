package com.vinay.monthlylekka.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context? = null) {
    private val prefs: SharedPreferences? = context?.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    private val _isFirstLaunch = MutableStateFlow(
        prefs?.getBoolean(KEY_IS_FIRST_LAUNCH, true) ?: true
    )
    val isFirstLaunchFlow: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    val isFirstLaunch: Boolean
        get() = _isFirstLaunch.value

    fun setFirstLaunchCompleted() {
        prefs?.edit()?.putBoolean(KEY_IS_FIRST_LAUNCH, false)?.apply()
        _isFirstLaunch.value = false
    }

    private val _monthStartDay = MutableStateFlow(
        prefs?.getInt(KEY_MONTH_START_DAY, DEFAULT_MONTH_START_DAY)?.coerceIn(MIN_DAY, MAX_DAY) ?: DEFAULT_MONTH_START_DAY
    )
    val monthStartDay: StateFlow<Int> = _monthStartDay.asStateFlow()

    fun getMonthStartDay(): Int {
        return _monthStartDay.value
    }

    fun setMonthStartDay(day: Int) {
        val validDay = day.coerceIn(MIN_DAY, MAX_DAY)
        prefs?.edit()?.putInt(KEY_MONTH_START_DAY, validDay)?.apply()
        _monthStartDay.value = validDay
    }

    private val _currencySymbol = MutableStateFlow(
        prefs?.getString(KEY_CURRENCY_SYMBOL, DEFAULT_CURRENCY_SYMBOL) ?: DEFAULT_CURRENCY_SYMBOL
    )
    val currencySymbolFlow: StateFlow<String> = _currencySymbol.asStateFlow()

    val currencySymbol: String
        get() = _currencySymbol.value

    fun updateCurrencySymbol(symbol: String) {
        val validSymbol = symbol.trim().ifEmpty { DEFAULT_CURRENCY_SYMBOL }
        prefs?.edit()?.putString(KEY_CURRENCY_SYMBOL, validSymbol)?.apply()
        _currencySymbol.value = validSymbol
    }

    companion object {
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_MONTH_START_DAY = "month_start_day"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val DEFAULT_MONTH_START_DAY = 1
        const val DEFAULT_CURRENCY_SYMBOL = "₹"
        const val MIN_DAY = 1
        const val MAX_DAY = 28
    }
}
