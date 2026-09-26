package com.shoropio.controlingreso.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shoropio.controlingreso.data.database.dao.AccessRecordDao
import com.shoropio.controlingreso.data.database.entity.AccessRecordEntity

@Database(
    entities = [AccessRecordEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accessRecordDao(): AccessRecordDao

    companion object {
        const val DB_NAME = "control_ingreso.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME).build()
    }
}