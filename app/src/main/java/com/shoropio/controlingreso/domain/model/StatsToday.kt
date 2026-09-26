package com.shoropio.controlingreso.domain.model

data class StatsToday(
    val entries: Int = 0,
    val exits: Int = 0,
    val insideCounts: Map<RecordType, Int> = emptyMap(),
) {
    val insideTotal: Int get() = insideCounts.values.sum()
}