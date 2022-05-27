package com.nhnextsoft.qrcode.db.dao

import androidx.room.*
import com.nhnextsoft.qrcode.db.entity.HistoryScan
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryScanDao {
    @Query("SELECT * FROM HistoryScan order by last_update DESC")
    fun getAll(): Flow<MutableList<HistoryScan>>

    @Query("SELECT * FROM HistoryScan WHERE id==:historyScanId order by last_update DESC limit 1")
    fun getItemById(historyScanId: Long): HistoryScan

    @Query("SELECT * FROM HistoryScan WHERE value_code==:content order by last_update DESC limit 1")
    suspend fun getItemByValueCode(content: String): HistoryScan?

    @Insert
    suspend fun insertAll(vararg historyScan: HistoryScan): List<Long>

    @Insert
    suspend fun insert(historyScan: HistoryScan): Long

    @Update
    fun update(historyScan: HistoryScan)

    @Delete
    suspend fun delete(historyScan: HistoryScan)

    @Query("DELETE FROM HistoryScan WHERE id = :historyScanId")
    suspend fun deleteById(historyScanId: Long)

    @Query("DELETE FROM HistoryScan")
    suspend fun deleteAll()
}