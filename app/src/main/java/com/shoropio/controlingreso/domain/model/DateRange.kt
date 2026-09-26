package com.shoropio.controlingreso.domain.model

enum class DateRange(val label: String) {
    ALL("Todos"),
    TODAY("Hoy"),
    YESTERDAY("Ayer"),
    LAST_7_DAYS("Últimos 7 días"),
    LAST_30_DAYS("Últimos 30 días"),
    CUSTOM("Personalizado")
}