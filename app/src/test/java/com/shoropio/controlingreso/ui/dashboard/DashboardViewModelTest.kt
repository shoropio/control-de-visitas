package com.shoropio.controlingreso.ui.dashboard

import com.shoropio.controlingreso.data.preferences.FakeSettingsRepository
import com.shoropio.controlingreso.data.repository.AccessRecordRepository
import com.shoropio.controlingreso.data.repository.FakeAccessRecordDao
import com.shoropio.controlingreso.domain.model.NewAccessRecord
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeAccessRecordDao()
    private val repository = AccessRecordRepository(dao)
    private val settings = FakeSettingsRepository()

    private fun entry(type: RecordType, name: String) = NewAccessRecord(
        type = type,
        fullName = name,
    )

    @Test
    fun `conteos y lista de dentro se actualizan`() = runTest {
        val viewModel = DashboardViewModel(repository, settings)
        val job = launch { viewModel.state.collect {} }

        repository.registerEntry(entry(RecordType.VISIT, "Juan Pérez"))
        repository.registerEntry(entry(RecordType.PROVIDER, "Ana Solís"))
        repository.registerEntry(entry(RecordType.TRUCKER, "Mario Rojas"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(3, state.insideTotal)
        assertEquals(1, state.counts[RecordType.VISIT])
        assertEquals(1, state.counts[RecordType.PROVIDER])
        assertEquals(1, state.counts[RecordType.TRUCKER])
        assertEquals(3, state.insideRecords.size)
        job.cancel()
    }

    @Test
    fun `registrar salida actualiza lista y conteo`() = runTest {
        val viewModel = DashboardViewModel(repository, settings)
        val job = launch { viewModel.state.collect {} }

        val record = repository.registerEntry(entry(RecordType.VISIT, "Juan Pérez"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.insideTotal)

        viewModel.registerExit(record.id, "Juan Pérez")
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.insideTotal)
        assertTrue(viewModel.state.value.insideRecords.isEmpty())
        job.cancel()
    }

    @Test
    fun `salida duplicada no se registra`() = runTest {
        val viewModel = DashboardViewModel(repository, settings)
        val job = launch { viewModel.state.collect {} }

        val record = repository.registerEntry(entry(RecordType.VISIT, "Juan"))
        viewModel.registerExit(record.id, "Juan")
        advanceUntilIdle()
        viewModel.registerExit(record.id, "Juan")
        advanceUntilIdle()

        val inside = repository.observeInside().first()
        assertTrue(inside.isEmpty())
        job.cancel()
    }

    @Test
    fun `búsqueda filtra la lista local`() = runTest {
        val viewModel = DashboardViewModel(repository, settings)
        val job = launch { viewModel.state.collect {} }

        repository.registerEntry(entry(RecordType.VISIT, "Juan Pérez"))
        repository.registerEntry(entry(RecordType.VISIT, "María López"))
        advanceUntilIdle()

        viewModel.onSearchChange("juan")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.insideRecords.size)
        assertEquals("Juan Pérez", viewModel.state.value.insideRecords.first().fullName)
        job.cancel()
    }
}