package com.shoropio.controlingreso.domain.model

data class AccessRecord(
    val id: Long,
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
    val entryDateTime: Long,
    val exitDateTime: Long?,
    val status: RecordStatus,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val secondaryLine: String
        get() = when (type) {
            RecordType.PROVIDER -> company.orEmpty()
            RecordType.TRUCKER -> containerNumber.orEmpty()
            RecordType.VISIT -> visitingPerson.orEmpty()
        }
}