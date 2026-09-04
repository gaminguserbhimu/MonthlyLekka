package com.vinay.monthlylekka.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("""
        SELECT c.* FROM categories c
        LEFT JOIN expenses e ON c.id = e.categoryId
        WHERE c.lekkaId = :lekkaId
        GROUP BY c.id
        ORDER BY c.isIncome DESC, COUNT(e.id) DESC, c.name ASC
    """)
    fun getCategoriesByLekka(lekkaId: Long): Flow<List<Category>>

    @Query("""
        SELECT c.* FROM categories c
        LEFT JOIN expenses e ON c.id = e.categoryId
        WHERE c.lekkaId = :lekkaId
        GROUP BY c.id
        ORDER BY c.isIncome DESC, COUNT(e.id) DESC, c.name ASC
    """)
    fun getAllCategories(lekkaId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    fun getAllCategoriesList(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}
