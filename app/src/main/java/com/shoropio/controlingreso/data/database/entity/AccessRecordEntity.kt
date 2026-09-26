package com.shoropio.controlingreso.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType

@Entity(tableName = "access_records")
data class AccessRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: RecordType,
    val fullName: String,
    val idNumber: String? = null,
    val licensePlate: String? = null,
    val company: String? = null,
    val visitingPerson: String? = null,
    val containerNumber: String? = null,
    val sealNumber: String? = null,
    val dispatcher: String? = null,
    val observations: String? = null,
    val entryDateTime: Long,
    val exitDateTime: Long? = null,
    val status: RecordStatus,
    val createdAt: Long,
    val updatedAt: Long,
)