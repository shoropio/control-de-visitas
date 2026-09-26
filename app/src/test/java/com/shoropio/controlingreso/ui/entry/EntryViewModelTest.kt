package com.shoropio.controlingreso.ui.entry

import androidx.lifecycle.SavedStateHandle
import com.shoropio.controlingreso.data.preferences.FakeSettingsRepository
import com.shoropio.controlingreso.data.repository.AccessRecordRepository
import com.shoropio.controlingreso.data.repository.FakeAccessRecordDao
import com.shoropio.controlingreso.domain.model.NewAccessRecord
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeAccessRecordDao()
    private val repository = AccessRecordRepository(dao)
    private val settings = FakeSettingsRepository()

    private fun viewModel(type: RecordType = RecordType.VISIT): EntryViewModel {
        val vm = EntryViewModel(repository, settings, SavedStateHandle(mapOf("type" to type.name)))
        // Suscribir al estado para que las modificaciones se propaguen.
        launch { vm.state.collect {} }
        return vm
    }

    @Test
    fun `registrar entrada crea un registro DENTRO`() = runTest {
        val vm = viewModel(RecordType.TRUCKER)
        val job = launch { vm.state.collect {} }

        vm.onFieldChange(EntryField.FULL_NAME, "Juan Pérez")
        vm.onFieldChange(EntryField.CEDULA, "1-1234-5678")
        vm.onFieldChange(EntryField.PLACA, "ABC123")
        vm.onFieldChange(EntryField.CONTENEDOR, "MSCU1234567")
        vm.onFieldChange(EntryField.MARCHAMO, "CR987654")
        vm.onFieldChange(EntryField.DESPACHADOR, "Carlos R.")
        vm.submit()
        advanceUntilIdle()

        val saved = vm.state.value.savedRecord
        assertNotNull(saved)
        assertEquals(RecordStatus.INSIDE, saved!!.status)
        assertEquals(RecordType.TRUCKER, saved.type)
        assertEquals("MSCU1234567", saved.containerNumber)
        assertEquals("CR987654", saved.sealNumber)
        assertEquals("Carlos R.", saved.dispatcher)
        job.cancel()
    }

    @Test
    fun `validación detiene registro sin nombre`() = runTest {
        val vm = viewModel()
        vm.submit()
        advanceUntilIdle()

        assertTrue(vm.state.value.fieldErrors.containsKey(EntryField.FULL_NAME.key))
        assertNull(vm.state.value.savedRecord)

        val all = repository.observeAll()
        val count = kotlinx.coroutines.flow.first(all)
        assertTrue(count.isEmpty())
    }

    @Test
    fun `cédula inválida bloquea el ingreso`() = runTest {
        val vm = viewModel()
        vm.onFieldChange(EntryField.FULL_NAME, "Ana Solís")
        vm.onFieldChange(EntryField.CEDULA, "xyz")
        vm.submit()
        advanceUntilIdle()

        assertTrue(vm.state.value.fieldErrors.containsKey(EntryField.CEDULA.key))
        assertNull(vm.state.value.savedRecord)
    }

    @Test
    fun `autocompletado por cédula sugiere datos guardados`() = runTest {
        repository.registerEntry(
            NewAccessRecord(
                type = RecordType.PROVIDER,
                fullName = "Juan Pérez",
                idNumber = "1-1234-5678",
                company = "Transportes Pérez",
                licensePlate = "ABC123",
            ),
        )

        val vm = viewModel(RecordType.PROVIDER)
        val job = launch { vm.state.collect {} }

        vm.onFieldChange(EntryField.CEDULA, "1-1234-5678")
        advanceTimeBy(300)
        advanceUntilIdle()

        val match = vm.state.value.knownMatch
        assertNotNull(match)
        assertEquals("Juan Pérez", match!!.fullName)

        vm.useKnownMatch()
        val stateAfter = vm.state.value
        assertEquals("Juan Pérez", stateAfter.fullName)
        assertEquals("Transportes Pérez", stateAfter.empresa)
        job.cancel()
    }

    @Test
    fun `proveedor requiere empresa`() = runTest {
        val vm = viewModel(RecordType.PROVIDER)
        vm.onFieldChange(EntryField.FULL_NAME, "Ana Solís")
        vm.submit()
        advanceUntilIdle()

        assertTrue(vm.state.value.fieldErrors.containsKey(EntryField.EMPRESA.key))
        assertNull(vm.state.value.savedRecord)
    }

    @Test
    fun `visita sin vehículo es válida`() = runTest {
        val vm = viewModel(RecordType.VISIT)
        val job = launch { vm.state.collect {} }

        vm.onFieldChange(EntryField.FULL_NAME, "María López")
        vm.onFieldChange(EntryField.VISITA_A, "Carlos R.")
        vm.submit()
        advanceUntilIdle()

        assertNotNull(vm.state.value.savedRecord)
        assertEquals(null, vm.state.value.savedRecord!!.licensePlate)
        job.cancel()
    }
}