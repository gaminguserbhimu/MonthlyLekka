package com.vinay.monthlylekka

import android.app.Application
import com.vinay.monthlylekka.data.AppDatabase
import com.vinay.monthlylekka.data.AppRepository
import com.vinay.monthlylekka.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MonthlyLekkaApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { AppRepository(database.categoryDao(), database.expenseDao(), database.lekkaDao(), database) }
    val userPreferences by lazy { UserPreferences(this) }
}
