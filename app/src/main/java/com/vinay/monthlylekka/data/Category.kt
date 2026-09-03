package com.vinay.monthlylekka.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = Lekka::class,
            parentColumns = ["id"],
            childColumns = ["lekkaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lekkaId")]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lekkaId: Long = 0,
    val name: String,
    val colorHex: String,
    val isIncome: Boolean
)

data class CategorySpec(
    val name: String,
    val colorHex: String,
    val isIncome: Boolean,
    val isDefault: Boolean = true
)

val DEFAULT_CATEGORY_SPECS = listOf(
    CategorySpec("Kirani", "#FFB300", isIncome = false),
    CategorySpec("Kaipalle", "#43A047", isIncome = false),
    CategorySpec("Food", "#E53935", isIncome = false),
    CategorySpec("Bills", "#3949AB", isIncome = false),
    CategorySpec("Others", "#757575", isIncome = false),
    CategorySpec("Travel", "#1E88E5", isIncome = false),
    CategorySpec("Hospital", "#D81B60", isIncome = false),
    CategorySpec("Income", "#2E7D32", isIncome = true)
)

