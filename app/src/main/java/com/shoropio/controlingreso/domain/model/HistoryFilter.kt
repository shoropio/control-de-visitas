package com.shoropio.controlingreso.domain.model

data class HistoryFilter(
    val range: DateRange = DateRange.ALL,
    val customStart: Long? = null,
    val customEnd: Long? = null,
    val type: RecordType? = null,
    val status: RecordStatus? = null,
)