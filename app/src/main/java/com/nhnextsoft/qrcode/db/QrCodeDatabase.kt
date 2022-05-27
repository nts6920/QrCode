package com.nhnextsoft.qrcode.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nhnextsoft.qrcode.db.dao.HistoryCreateCodeDao
import com.nhnextsoft.qrcode.db.dao.HistoryScanDao
import com.nhnextsoft.qrcode.db.entity.HistoryCreateCode
import com.nhnextsoft.qrcode.db.entity.HistoryScan

@Database(
    entities = [HistoryScan::class, HistoryCreateCode::class], version = 1, exportSchema = true,

)
@TypeConverters(DatabaseConverters::class)
abstract class QrCodeDatabase : RoomDatabase() {
    abstract fun historyScanDao(): HistoryScanDao
    abstract fun historyCreateCodeDao(): HistoryCreateCodeDao
}