package com.vinay.monthlylekka.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LekkaDao {
    @Query("SELECT * FROM lekkas")
    fun getAllLekkas(): Flow<List<Lekka>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLekka(lekka: Lekka): Long

    @Update
    suspend fun updateLekka(lekka: Lekka)

    @Delete
    suspend fun deleteLekka(lekka: Lekka)

    @Query("SELECT * FROM lekkas WHERE id = :id")
    suspend fun getLekkaById(id: Long): Lekka?

    @Query("SELECT COUNT(*) FROM lekkas")
    suspend fun getLekkaCount(): Int

    @Query("UPDATE lekkas SET isDefault = CASE WHEN id = :lekkaId THEN 1 ELSE 0 END")
    suspend fun setDefaultLekka(lekkaId: Long)

    @Query("SELECT * FROM lekkas WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultLekka(): Lekka?

    @Query("SELECT * FROM lekkas WHERE isMotherTable = 1 LIMIT 1")
    suspend fun getMotherTable(): Lekka?

    @Query("UPDATE lekkas SET name = 'Master Expense Table' WHERE isMotherTable = 1")
    suspend fun updateMotherTableName()

    @Query("SELECT * FROM lekkas WHERE isMotherTable = 0")
    fun getChildLekkas(): Flow<List<Lekka>>

    @Query("DELETE FROM lekkas")
    suspend fun deleteAllLekkas()
}
