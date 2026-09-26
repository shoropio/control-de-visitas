package com.shoropio.controlingreso.data.database

import androidx.room.TypeConverter
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType

class Converters {

    @TypeConverter
    fun typeToString(value: RecordType?): String? = value?.name

    @TypeConverter
    fun stringToType(value: String?): RecordType? = value?.let { RecordType.valueOf(it) }

    @TypeConverter
    fun statusToString(value: RecordStatus?): String? = value?.name

    @TypeConverter
    fun stringToStatus(value: String?): RecordStatus? = value?.let { RecordStatus.valueOf(it) }
}