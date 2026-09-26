package com.shoropio.controlingreso.data.csv

import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.utils.Formatters

object CsvExporter {

    private const val DELIMITER = ";"
    private const val NEW_LINE = "\r\n"
    private const val BOM = "\uFEFF"

    private val HEADER = listOf(
        "Tipo", "Nombre", "Cédula", "Placa", "Empresa", "Visita a",
        "Contenedor", "Marchamo", "Despachador", "Observaciones",
        "Fecha entrada", "Hora entrada", "Fecha salida", "Hora salida", "Estado",
    ).joinToString(DELIMITER)

    fun build(
        records: List<AccessRecord>,
        datePattern: String = "dd/MM/yyyy",
        use24Hour: Boolean = true,
    ): String {
        val sb = StringBuilder(BOM)
        sb.append(HEADER).append(NEW_LINE)
        records.forEach { record ->
            sb.append(buildRow(record, datePattern, use24Hour)).append(NEW_LINE)
        }
        return sb.toString()
    }

    fun computeRowCount(records: List<AccessRecord>): Int = records.size + 1

    private fun buildRow(
        record: AccessRecord,
        datePattern: String,
        use24Hour: Boolean,
    ): String {
        val entryDate = Formatters.formatDate(record.entryDateTime, datePattern)
        val entryTime = Formatters.formatTime(record.entryDateTime, use24Hour)
        val exitDate = record.exitDateTime?.let { Formatters.formatDate(it, datePattern) }.orEmpty()
        val exitTime = record.exitDateTime?.let { Formatters.formatTime(it, use24Hour) }.orEmpty()

        return listOf(
            record.type.label,
            record.fullName,
            record.idNumber,
            record.licensePlate,
            record.company,
            record.visitingPerson,
            record.containerNumber,
            record.sealNumber,
            record.dispatcher,
            record.observations,
            entryDate,
            entryTime,
            exitDate,
            exitTime,
            record.status.label,
        ).joinToString(DELIMITER) { esc(it) }
    }

    private fun esc(value: String?): String {
        val s = value.orEmpty()
        val needsQuotes = s.contains(DELIMITER) || s.contains('"') || s.contains('\n') || s.contains('\r')
        return if (needsQuotes) "\"" + s.replace("\"", "\"\"") + "\"" else s
    }
}