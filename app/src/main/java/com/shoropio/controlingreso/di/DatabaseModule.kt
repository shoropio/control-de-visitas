package com.shoropio.controlingreso.di

import android.content.Context
import androidx.room.Room
import com.shoropio.controlingreso.data.database.AppDatabase
import com.shoropio.controlingreso.data.database.dao.AccessRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME).build()

    @Provides
    fun provideDao(database: AppDatabase): AccessRecordDao = database.accessRecordDao()
}