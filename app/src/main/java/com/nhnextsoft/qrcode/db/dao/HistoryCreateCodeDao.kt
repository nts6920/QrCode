package com.nhnextsoft.qrcode.db.dao

import androidx.room.*
import com.nhnextsoft.qrcode.db.entity.HistoryCreateCode
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryCreateCodeDao {
    @Query("SELECT * FROM HistoryCreateCode order by last_update DESC")
    fun getAll(): Flow<MutableList<HistoryCreateCode>>

    @Query("SELECT * FROM HistoryCreateCode WHERE value_code==:valueCode order by last_update DESC limit 1")
    suspend fun getItemByValueCode(valueCode: String): HistoryCreateCode?

    @Query("SELECT * FROM HistoryCreateCode WHERE id==:historyCreateID order by last_update DESC limit 1")
    fun getItemById(historyCreateID: Long): HistoryCreateCode

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg historyCreateCode: HistoryCreateCode): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(historyCreateCode: HistoryCreateCode): Long

    @Delete
    suspend fun delete(historyCreateCode: HistoryCreateCode)

    @Query("DELETE FROM HistoryCreateCode WHERE id = :historyCreateCodeId")
    suspend fun deleteById(historyCreateCodeId: Long)

    @Query("DELETE FROM HistoryCreateCode")
    suspend fun deleteAll()

}