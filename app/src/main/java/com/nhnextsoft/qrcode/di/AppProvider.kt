package com.nhnextsoft.qrcode.di

import android.content.Context
import androidx.room.Room
import com.nhnextsoft.qrcode.db.QrCodeDatabase
import com.nhnextsoft.qrcode.db.dao.HistoryCreateCodeDao
import com.nhnextsoft.qrcode.db.dao.HistoryScanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppProvider {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext application: Context): QrCodeDatabase {
        return Room
            .databaseBuilder(application, QrCodeDatabase::class.java, "qrcode-database")
            .build()
    }

    @Provides
    @Singleton
    fun provideHistoryScanDao(qrCodeDatabase: QrCodeDatabase) : HistoryScanDao {
        return qrCodeDatabase.historyScanDao()
    }

    @Provides
    @Singleton
    fun provideHistoryCreateCodeDao(qrCodeDatabase: QrCodeDatabase) : HistoryCreateCodeDao {
        return qrCodeDatabase.historyCreateCodeDao()
    }
}