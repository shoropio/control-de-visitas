package com.shoropio.controlingreso.utils

import com.shoropio.controlingreso.domain.model.DateRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object TimeWindow {

    private fun zone(): ZoneId = ZoneId.systemDefault()

    fun startOfDay(epochMillis: Long): Long =
        LocalDate.ofInstant(Instant.ofEpochMilli(epochMillis), zone())
            .atStartOfDay(zone())
            .toInstant()
            .toEpochMilli()

    fun endOfDay(epochMillis: Long): Long = startOfDay(epochMillis) + 86_400_000L - 1L

    fun startOfYesterday(epochMillis: Long): Long = startOfDay(epochMillis) - 86_400_000L

    fun daysAgoStart(epochMillis: Long, days: Int): Long = startOfDay(epochMillis) - days * 86_400_000L

    fun isToday(epochMillis: Long, now: Long = System.currentTimeMillis()): Boolean =
        epochMillis in startOfDay(now)..endOfDay(now)

    /**
     * Devuelve [start, end] en epoch millis según el rango seleccionado.
     * Para [DateRange.ALL] devuelve (null, null).
     */
    fun resolveBounds(
        range: DateRange,
        now: Long = System.currentTimeMillis(),
        customStart: Long? = null,
        customEnd: Long? = null,
    ): Pair<Long?, Long?> = when (range) {
        DateRange.ALL -> null to null
        DateRange.TODAY -> startOfDay(now) to endOfDay(now)
        DateRange.YESTERDAY -> startOfYesterday(now) to endOfDay(startOfYesterday(now))
        DateRange.LAST_7_DAYS -> daysAgoStart(now, 6) to endOfDay(now)
        DateRange.LAST_30_DAYS -> daysAgoStart(now, 29) to endOfDay(now)
        DateRange.CUSTOM -> (customStart ?: startOfDay(now)) to (customEnd ?: endOfDay(now))
    }
}