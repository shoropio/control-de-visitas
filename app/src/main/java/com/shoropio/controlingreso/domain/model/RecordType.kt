package com.shoropio.controlingreso.domain.model

enum class RecordType(val label: String) {
    VISIT("Visita"),
    PROVIDER("Proveedor"),
    TRUCKER("Trailero");

    companion object {
        fun fromName(name: String): RecordType? = entries.firstOrNull { it.name == name }
    }
}