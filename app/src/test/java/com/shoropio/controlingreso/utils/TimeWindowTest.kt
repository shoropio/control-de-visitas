package com.shoropio.controlingreso.utils

import com.shoropio.controlingreso.domain.model.DateRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeWindowTest {

    private val zone = ZoneId.systemDefault()

    private fun fixedNow() = LocalDate.of(2026, 9, 21)
        .atTime(10, 30)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()

    private fun epoch(date: LocalDate, hour: Int = 0): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli() + hour * 3_600_000L

    @Test
    fun `hoy abarca el día completo`() {
        val now = fixedNow()
        val (start, end) = TimeWindow.resolveBounds(DateRange.TODAY, now)
        assertEquals(epoch(LocalDate.of(2026, 9, 21)), start)
        assertEquals(epoch(LocalDate.of(2026, 9, 22)) - 1, end)
    }

    @Test
    fun `ayer cubre el día anterior`() {
        val now = fixedNow()
        val (start, end) = TimeWindow.resolveBounds(DateRange.YESTERDAY, now)
        assertEquals(epoch(LocalDate.of(2026, 9, 20)), start)
        assertEquals(epoch(LocalDate.of(2026, 9, 21)) - 1, end)
    }

    @Test
    fun `últimos 7 días incluye 6 días atrás hasta hoy`() {
        val now = fixedNow()
        val (start, end) = TimeWindow.resolveBounds(DateRange.LAST_7_DAYS, now)
        assertEquals(epoch(LocalDate.of(2026, 9, 15)), start)
        assertEquals(epoch(LocalDate.of(2026, 9, 22)) - 1, end)
    }

    @Test
    fun `últimos 30 días incluye 29 días atrás`() {
        val now = fixedNow()
        val (start, _) = TimeWindow.resolveBounds(DateRange.LAST_30_DAYS, now)
        assertEquals(epoch(LocalDate.of(2026, 8, 23)), start)
    }

    @Test
    fun `rango custom usa valores dados`() {
        val now = fixedNow()
        val a = epoch(LocalDate.of(2026, 8, 1))
        val b = epoch(LocalDate.of(2026, 8, 31))
        val (start, end) = TimeWindow.resolveBounds(DateRange.CUSTOM, now, a, b)
        assertEquals(a, start)
        assertEquals(b, end)
    }

    @Test
    fun `todos no filtra`() {
        val (start, end) = TimeWindow.resolveBounds(DateRange.ALL, fixedNow())
        assertEquals(null, start)
        assertEquals(null, end)
    }

    @Test
    fun `isToday distingue días`() {
        val now = fixedNow()
        assertTrue(TimeWindow.isToday(now, now))
        assertTrue(!TimeWindow.isToday(epoch(LocalDate.of(2026, 9, 19), 9), now))
    }

    @Test
    fun `epoch corresponde al inicio local`() {
        // Verifica que startOfDay devuelve un múltiplo de día local.
        val start = TimeWindow.startOfDay(fixedNow())
        assertEquals(
            Instant.ofEpochMilli(start).atZone(zone).hour,
            0,
        )
    }
}