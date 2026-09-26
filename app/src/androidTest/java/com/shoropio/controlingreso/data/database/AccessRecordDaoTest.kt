package com.shoropio.controlingreso.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shoropio.controlingreso.data.database.dao.AccessRecordDao
import com.shoropio.controlingreso.data.database.entity.AccessRecordEntity
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessRecordDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AccessRecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.accessRecordDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun entity(
        type: RecordType = RecordType.VISIT,
        name: String = "Juan Pérez",
        entry: Long = System.currentTimeMillis(),
        status: RecordStatus = RecordStatus.INSIDE,
    ) = AccessRecordEntity(
        type = type,
        fullName = name,
        idNumber = "1-1234-5678",
        licensePlate = "ABC123",
        company = "Transportes XYZ",
        entryDateTime = entry,
        exitDateTime = null,
        status = status,
        createdAt = entry,
        updatedAt = entry,
    )

    @Test
    fun insert_and_observeInside() = runBlocking {
        dao.insert(entity())
        val inside = dao.observeInside().first()
        assertEquals(1, inside.size)
        assertEquals(RecordType.VISIT, inside.first().type)
    }

    @Test
    fun search_finds_by_name_and_plate() = runBlocking {
        dao.insert(entity(name = "Juan Pérez"))
        dao.insert(entity(name = "María López", entry = System.currentTimeMillis() + 1))

        val byName = dao.search("juan", null, null, null, null).first()
        assertEquals(1, byName.size)
        assertEquals("Juan Pérez", byName.first().fullName)

        val byPlate = dao.search("abc", null, null, null, null).first()
        assertEquals(2, byPlate.size)
    }

    @Test
    fun search_filters_by_status_and_type() = runBlocking {
        val visit = dao.insert(entity(name = "Visitante"))
        dao.insert(entity(type = RecordType.TRUCKER, name = "Trailero", company = null))
        dao.registerExit(visit, System.currentTimeMillis() + 1, System.currentTimeMillis() + 1)

        val salidos = dao.search(null, null, null, RecordStatus.EXITED, null).first()
        assertEquals(1, salidos.size)
        assertEquals("Visitante", salidos.first().fullName)

        val truckers = dao.search(null, null, null, null, RecordType.TRUCKER).first()
        assertEquals(1, truckers.size)
        assertEquals("Trailero", truckers.first().fullName)
    }

    @Test
    fun no_double_exit() = runBlocking {
        val id = dao.insert(entity())
        assertEquals(1, dao.registerExit(id, System.currentTimeMillis() + 1, System.currentTimeMillis() + 1))
        assertEquals(0, dao.registerExit(id, System.currentTimeMillis() + 2, System.currentTimeMillis() + 2))
    }

    @Test
    fun inside_counts_grouped_by_type() = runBlocking {
        dao.insert(entity(type = RecordType.PROVIDER, name = "Proveedor"))
        dao.insert(entity(type = RecordType.TRUCKER, name = "Trailero1", company = null))
        dao.insert(entity(type = RecordType.TRUCKER, name = "Trailero2", company = null))

        val counts = dao.observeInsideCounts().first()
        assertTrue(counts.any { it.type == RecordType.PROVIDER && it.count == 1 })
        assertTrue(counts.any { it.type == RecordType.TRUCKER && it.count == 2 })
    }

    @Test
    fun count_entries_by_day() = runBlocking {
        val today = System.currentTimeMillis()
        dao.insert(entity(name = "A", entry = today))
        dao.insert(entity(name = "B", entry = today - 200_000L))
        dao.insert(entity(name = "Antiguo", entry = today - 3 * 86_400_000L))

        val start = today - 300_000L
        val end = today + 60_000L
        assertEquals(2, dao.countEntriesBetween(start, end))
        assertFalse(false)
    }
}