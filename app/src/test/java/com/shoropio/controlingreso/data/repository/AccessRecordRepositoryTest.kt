package com.shoropio.controlingreso.data.repository

import com.shoropio.controlingreso.domain.model.DateRange
import com.shoropio.controlingreso.domain.model.HistoryFilter
import com.shoropio.controlingreso.domain.model.NewAccessRecord
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.domain.model.ReportFilter
import com.shoropio.controlingreso.utils.TimeWindow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessRecordRepositoryTest {

    private val dao = FakeAccessRecordDao()
    private val repository = AccessRecordRepository(dao)

    private fun visit(name: String = "Juan Pérez", cedula: String? = null, plate: String? = null) =
        NewAccessRecord(
            type = RecordType.VISIT,
            fullName = name,
            idNumber = cedula,
            licensePlate = plate,
        )

    private fun provider(name: String, company: String? = null) = NewAccessRecord(
        type = RecordType.PROVIDER,
        fullName = name,
        company = company,
    )

    private fun trucker(name: String, container: String? = null) = NewAccessRecord(
        type = RecordType.TRUCKER,
        fullName = name,
        containerNumber = container,
        dispatcher = "Pedro",
    )

    @Test
    fun `crear entrada guarda fecha hora y estado DENTRO`() = runTest {
        val record = repository.registerEntry(visit(cedula = "1-1234-5678", plate = "ABC123"))
        assertEquals(RecordStatus.INSIDE, record.status)
        assertTrue(record.entryDateTime > 0)
        assertNull(record.exitDateTime)
        assertTrue(record.createdAt == record.entryDateTime)

        val inside = repository.observeInside().first()
        assertEquals(1, inside.size)
        assertEquals("ABC123", inside.first().licensePlate)
    }

    @Test
    fun `registrar salida guarda hora y cambia estado`() = runTest {
        val record = repository.registerEntry(visit())
        assertTrue(repository.registerExit(record.id))

        val updated = repository.observeById(record.id).first()
        assertNotNull(updated)
        assertEquals(RecordStatus.EXITED, updated!!.status)
        assertNotNull(updated.exitDateTime)
        assertTrue(updated.exitDateTime!! >= record.entryDateTime)

        val inside = repository.observeInside().first()
        assertTrue(inside.isEmpty())
    }

    @Test
    fun `no permite doble salida`() = runTest {
        val record = repository.registerEntry(visit())
        assertTrue(repository.registerExit(record.id))
        assertFalse(repository.registerExit(record.id))
    }

    @Test
    fun `buscar por nombre`() = runTest {
        repository.registerEntry(visit("Juan Pérez"))
        repository.registerEntry(visit("María López"))
        val result = repository.search("Juan", DateRange.ALL, null, null, null, null).first()
        assertEquals(1, result.size)
        assertEquals("Juan Pérez", result.first().fullName)
    }

    @Test
    fun `buscar por cedula`() = runTest {
        repository.registerEntry(visit(cedula = "1-1234-5678"))
        repository.registerEntry(visit(cedula = "2-2222-2222"))
        val result = repository.search("1234", DateRange.ALL, null, null, null, null).first()
        assertEquals(1, result.size)
        assertEquals("1-1234-5678", result.first().idNumber)
    }

    @Test
    fun `buscar por placa`() = runTest {
        repository.registerEntry(visit(plate = "ABC123"))
        repository.registerEntry(visit(plate = "XYZ999"))
        val result = repository.search("abc1", DateRange.ALL, null, null, null, null).first()
        assertEquals(1, result.size)
        assertEquals("ABC123", result.first().licensePlate)
    }

    @Test
    fun `buscar por empresa`() = runTest {
        repository.registerEntry(provider("Ana Solís", "Transportes XYZ"))
        repository.registerEntry(provider("Carla Mora", "Banana Export"))
        val result = repository.search("transportes", DateRange.ALL, null, null, null, null).first()
        assertEquals(1, result.size)
        assertEquals("Ana Solís", result.first().fullName)
    }

    @Test
    fun `buscar por contenedor`() = runTest {
        repository.registerEntry(trucker("Juan", "MSCU1234567"))
        repository.registerEntry(trucker("Mario", "MAEU9999999"))
        val result = repository.search("mscu", DateRange.ALL, null, null, null, null).first()
        assertEquals(1, result.size)
        assertEquals("MSCU1234567", result.first().containerNumber)
    }

    @Test
    fun `filtrar por tipo y estado`() = runTest {
        val record = repository.registerEntry(visit("Visitante"))
        repository.registerEntry(provider("ProveedorX", "Empresa Y"))
        repository.registerExit(record.id)

        val visitas = repository.search(HistoryFilter(type = RecordType.VISIT), "").first()
        assertEquals(1, visitas.size)
        assertEquals(RecordType.VISIT, visitas.first().type)

        val salidos = repository.search(HistoryFilter(status = RecordStatus.EXITED), "").first()
        assertEquals(1, salidos.size)
        assertEquals("Visitante", salidos.first().fullName)
    }

    @Test
    fun `filtrar por fecha hoy`() = runTest {
        repository.registerEntry(visit("Hoy"))
        val result = repository.search(HistoryFilter(range = DateRange.TODAY), "").first()
        assertEquals(1, result.size)
    }

    @Test
    fun `obtener personas dentro y conteos del dashboard`() = runTest {
        repository.registerEntry(visit()) // persona 1
        val juan = repository.registerEntry(provider("Juan"))
        repository.registerEntry(trucker("Trailero"))
        repository.registerExit(juan.id)

        val counts = repository.observeInsideCounts().first()
        assertEquals(1, counts[RecordType.VISIT])
        assertEquals(null, counts[RecordType.PROVIDER])
        assertEquals(1, counts[RecordType.TRUCKER])
        assertEquals(2, counts.values.sum())

        val inside = repository.observeInside().first()
        assertEquals(2, inside.size)
    }

    @Test
    fun `limpiar registros antiguos solo elimina salidos`() = runTest {
        val active = repository.registerEntry(visit("Activo"))
        val reg = repository.registerEntry(visit("Salido"))
        repository.registerExit(reg.id)

        val old = System.currentTimeMillis() - 60 * 86_400_000L
        val deleted = repository.deleteOldExited(old)
        assertEquals(1, deleted)

        val all = repository.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("Activo", all.first().fullName)
        assertTrue(active.id != reg.id)
    }

    @Test
    fun `reportes por rango`() = runTest {
        val now = System.currentTimeMillis()
        repository.registerEntry(visit("Reporte A"))
        val start = TimeWindow.startOfDay(now)
        val end = TimeWindow.endOfDay(now)
        val records = repository.getReportRecords(ReportFilter(start, end))
        assertEquals(1, records.size)
    }

    @Test
    fun `autocompletado encuentra persona conocida por cedula`() = runTest {
        repository.registerEntry(provider("Juan Pérez", "Transportes Pérez"))
        val known = repository.findKnownByCedula("  1-1234-5678  ")
        assertNull(known)

        repository.registerEntry(
            NewAccessRecord(
                type = RecordType.PROVIDER,
                fullName = "Juan Pérez",
                idNumber = "1-1234-5678",
                company = "Transportes Pérez",
                licensePlate = "ABC123",
            ),
        )
        val found = repository.findKnownByCedula("1-1234-5678")
        assertNotNull(found)
        assertEquals("Transportes Pérez", found!!.company)
    }
}