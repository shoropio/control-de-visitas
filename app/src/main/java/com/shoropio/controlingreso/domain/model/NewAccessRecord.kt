package com.shoropio.controlingreso.domain.model

data class NewAccessRecord(
    val type: RecordType,
    val fullName: String,
    val idNumber: String?,
    val licensePlate: String?,
    val company: String?,
    val visitingPerson: String?,
    val containerNumber: String?,
    val sealNumber: String?,
    val dispatcher: String?,
    val observations: String?,
)