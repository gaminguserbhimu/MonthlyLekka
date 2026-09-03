package com.vinay.monthlylekka.ui

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

sealed interface Route : NavKey, Parcelable {
    @Serializable
    @Parcelize
    data object Welcome : Route

    @Serializable
    @Parcelize
    data object Tables : Route

    @Serializable
    @Parcelize
    data class Dashboard(val lekkaId: Long) : Route

    @Serializable
    @Parcelize
    data class TableDetail(val lekkaId: Long) : Route

    @Serializable
    @Parcelize
    data class AddExpense(val lekkaId: Long, val expenseId: Long? = null) : Route

    @Serializable
    @Parcelize
    data class CategoryManagement(val lekkaId: Long) : Route

    @Serializable
    @Parcelize
    data class Analytics(val lekkaId: Long) : Route
}
