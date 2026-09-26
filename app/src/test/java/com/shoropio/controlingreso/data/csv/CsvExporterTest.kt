package com.shoropio.controlingreso.data.csv

import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {

    private fun record(
        id: Long,
        type: RecordType,
        name: String,
        observations: String? = null,
        exited: Boolean = false,
    ): AccessRecord {
        val base = 1_700_000_000_000L
        return AccessRecord(
            id = id,
            type = type,
            fullName = name,
            idNumber = "1-1234-5678",
            licensePlate = "ABC123",
            company = if (type == RecordType.PROVIDER) "Transportes;XYZ" else null,
            visitingPerson = if (type == RecordType.VISIT) "Carlos" else null,
            containerNumber = if (type == RecordType.TRUCKER) "MSCU1234567" else null,
            sealNumber = if (type == RecordType.TRUCKER) "CR987654" else null,
            dispatcher = if (type == RecordType.TRUCKER) "Pedro" else null,
            observations = observations,
            entryDateTime = base,
            exitDateTime = if (exited) base + 3_600_000L else null,
            status = if (exited) RecordStatus.EXITED else RecordStatus.INSIDE,
            createdAt = base,
            updatedAt = base,
        )
    }

    @Test
    fun `csv incluye encabezado y una fila`() {
        val csv = CsvExporter.build(listOf(record(1, RecordType.VISIT, "Juan Pérez")))
        val lines = csv.trim().split("\r\n")
        assertEquals(2, lines.size)
        assertTrue(lines[0].contains("Tipo;Nombre;Cédula;Placa"))
        assertTrue(lines[1].contains("Visita;Juan Pérez"))
    }

    @Test
    fun `csv empieza con BOM UTF-8`() {
        val csv = CsvExporter.build(listOf(record(1, RecordType.VISIT, "Juan")))
        assertTrue(csv.startsWith("\uFEFF"))
    }

    @Test
    fun `campos con separador se escapan entre comillas`() {
        val csv = CsvExporter.build(
            listOf(record(1, RecordType.PROVIDER, "Ana", observations = "observación; importante; con \"comillas\"")),
        )
        val line = csv.lines()[1]
        assertTrue(line.contains("\"Transportes;XYZ\""))
        assertTrue(line.contains("\"observación; importante; con \"\"comillas\"\"\""))
    }

    @Test
    fun `trailero incluye contenedor y marchamo`() {
        val csv = CsvExporter.build(listOf(record(1, RecordType.TRUCKER, "Juan")))
        assertTrue(csv.contains("MSCU1234567"))
        assertTrue(csv.contains("CR987654"))
    }

    @Test
    fun `salida vacía cuando no hay salida registrada`() {
        val csv = CsvExporter.build(listOf(record(1, RecordType.VISIT, "Juan")))
        val cols = csv.lines()[1].split(";")
        assertEquals("", cols[12])
        assertEquals("", cols[13])
    }

    @Test
    fun `estado EXITED se refleja`() {
        val csv = CsvExporter.build(listOf(record(1, RecordType.VISIT, "Juan", exited = true)))
        assertTrue(csv.contains("Salido"))
    }
}