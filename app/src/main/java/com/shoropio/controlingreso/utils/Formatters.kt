package com.shoropio.controlingreso.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Formatters {

    private fun zone(): ZoneId = ZoneId.systemDefault()

    private fun toLocal(epochMillis: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zone())

    fun formatDate(epochMillis: Long, pattern: String = "dd/MM/yyyy"): String =
        toLocal(epochMillis).format(DateTimeFormatter.ofPattern(pattern))

    fun formatTime(epochMillis: Long, use24Hour: Boolean = true): String =
        toLocal(epochMillis).format(DateTimeFormatter.ofPattern(if (use24Hour) "HH:mm" else "hh:mm a"))

    fun formatDateTime(
        epochMillis: Long,
        datePattern: String = "dd/MM/yyyy",
        use24Hour: Boolean = true,
    ): String = "${formatDate(epochMillis, datePattern)} ${formatTime(epochMillis, use24Hour)}"

    fun formatFullDateTime(epochMillis: Long): String =
        toLocal(epochMillis).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))

    fun greeting(epochMillis: Long = System.currentTimeMillis()): String {
        val hour = toLocal(epochMillis).hour
        return when {
            hour in 5..11 -> "Buenos días"
            hour in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }
}