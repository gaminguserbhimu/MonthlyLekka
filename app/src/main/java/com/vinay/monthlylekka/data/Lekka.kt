package com.vinay.monthlylekka.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lekkas")
data class Lekka(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val isDefault: Boolean = false,
    val isMotherTable: Boolean = false
)
