package com.shoropio.controlingreso.domain.model

data class ReportFilter(
    val start: Long,
    val end: Long,
    val type: RecordType? = null,
    val status: RecordStatus? = null,
)