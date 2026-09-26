package com.shoropio.controlingreso.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoropio.controlingreso.data.preferences.AppSettings
import com.shoropio.controlingreso.data.preferences.SettingsRepository
import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.NewAccessRecord
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.domain.repository.AccessRepository
import com.shoropio.controlingreso.utils.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntryUiState(
    val type: RecordType,
    val fullName: String = "",
    val cedula: String = "",
    val placa: String = "",
    val empresa: String = "",
    val visitaA: String = "",
    val contenedor: String = "",
    val marchamo: String = "",
    val despachador: String = "",
    val observaciones: String = "",
    val knownMatch: AccessRecord? = null,
    val knownPlateMatch: AccessRecord? = null,
    val companies: List<String> = emptyList(),
    val visitingPersons: List<String> = emptyList(),
    val dispatchers: List<String> = emptyList(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val saving: Boolean = false,
    val savedRecord: AccessRecord? = null,
    val settings: AppSettings = AppSettings(),
)

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val repository: AccessRepository,
    private val preferences: SettingsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val type: RecordType =
        RecordType.fromName(savedStateHandle.get<String>("type") ?: "") ?: RecordType.VISIT

    private val form = MutableStateFlow(EntryUiState(type = type))

    val state: StateFlow<EntryUiState> =
        combine(form, preferences.settings) { s, settings -> s.copy(settings = settings) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntryUiState(type = type))

    private val cedulaQuery = MutableStateFlow("")
    private val placaQuery = MutableStateFlow("")
    private val companyQuery = MutableStateFlow("")
    private val visitorQuery = MutableStateFlow("")
    private val dispatcherQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            cedulaQuery
                .debounce(250)
                .distinctUntilChanged()
                .collectLatest { value ->
                    val match = if (value.isBlank()) null else repository.findKnownByCedula(value)
                    form.update { it.copy(knownMatch = match) }
                }
        }
        viewModelScope.launch {
            placaQuery
                .debounce(250)
                .distinctUntilChanged()
                .collectLatest { value ->
                    val match = if (value.isBlank()) null else repository.findKnownByPlate(value)
                    form.update { it.copy(knownPlateMatch = match) }
                }
        }
        viewModelScope.launch {
            companyQuery
                .debounce(200)
                .distinctUntilChanged()
                .flatMapLatest { prefix -> flow { emit(repository.findCompanies(prefix, 6)) } }
                .collect { list -> form.update { it.copy(companies = list) } }
        }
        viewModelScope.launch {
            visitorQuery
                .debounce(200)
                .distinctUntilChanged()
                .flatMapLatest { prefix -> flow { emit(repository.findVisitingPersons(prefix, 6)) } }
                .collect { list -> form.update { it.copy(visitingPersons = list) } }
        }
        viewModelScope.launch {
            dispatcherQuery
                .debounce(200)
                .distinctUntilChanged()
                .flatMapLatest { prefix -> flow { emit(repository.findDispatchers(prefix, 6)) } }
                .collect { list -> form.update { it.copy(dispatchers = list) } }
        }
    }

    fun onFieldChange(field: EntryField, value: String) {
        form.update { current ->
            val cleared = current.fieldErrors - field.key
            when (field) {
                EntryField.FULL_NAME -> current.copy(fullName = value, fieldErrors = cleared)
                EntryField.CEDULA -> {
                    cedulaQuery.value = value
                    current.copy(cedula = value, fieldErrors = cleared)
                }
                EntryField.PLACA -> {
                    placaQuery.value = value
                    current.copy(placa = value, fieldErrors = cleared)
                }
                EntryField.EMPRESA -> {
                    companyQuery.value = value
                    current.copy(empresa = value, fieldErrors = cleared)
                }
                EntryField.VISITA_A -> {
                    visitorQuery.value = value
                    current.copy(visitaA = value, fieldErrors = cleared)
                }
                EntryField.CONTENEDOR -> current.copy(contenedor = value, fieldErrors = cleared)
                EntryField.MARCHAMO -> current.copy(marchamo = value, fieldErrors = cleared)
                EntryField.DESPACHADOR -> {
                    dispatcherQuery.value = value
                    current.copy(despachador = value, fieldErrors = cleared)
                }
                EntryField.OBSERVACIONES -> current.copy(observaciones = value, fieldErrors = cleared)
            }
        }
    }

    fun useKnownMatch() {
        val match = form.value.knownMatch ?: return
        form.update { current ->
            current.copy(
                fullName = current.fullName.ifBlank { match.fullName },
                placa = current.placa.ifBlank { match.licensePlate.orEmpty() },
                empresa = current.empresa.ifBlank { match.company.orEmpty() },
                contenedor = current.contenedor.ifBlank { match.containerNumber.orEmpty() },
                marchamo = current.marchamo.ifBlank { match.sealNumber.orEmpty() },
                despachador = current.despachador.ifBlank { match.dispatcher.orEmpty() },
                visitaA = current.visitaA.ifBlank { match.visitingPerson.orEmpty() },
            )
        }
    }

    fun useKnownPlateMatch() {
        val match = form.value.knownPlateMatch ?: return
        form.update { current ->
            current.copy(
                fullName = current.fullName.ifBlank { match.fullName },
                contenedor = current.contenedor.ifBlank { match.containerNumber.orEmpty() },
                marchamo = current.marchamo.ifBlank { match.sealNumber.orEmpty() },
                despachador = current.despachador.ifBlank { match.dispatcher.orEmpty() },
            )
        }
    }

    fun selectCompany(value: String) {
        form.update { it.copy(empresa = value, companies = emptyList(), fieldErrors = it.fieldErrors - "empresa") }
    }

    fun selectVisitingPerson(value: String) {
        form.update { it.copy(visitaA = value, visitingPersons = emptyList(), fieldErrors = it.fieldErrors - "visitaA") }
    }

    fun selectDispatcher(value: String) {
        form.update { it.copy(despachador = value, dispatchers = emptyList(), fieldErrors = it.fieldErrors - "despachador") }
    }

    fun submit() {
        val current = form.value
        if (current.saving || current.savedRecord != null) return

        val errors = buildMap {
            Validation.validateName(current.fullName)?.let { put(EntryField.FULL_NAME.key, it) }
            Validation.validateCedula(current.cedula)?.let { put(EntryField.CEDULA.key, it) }
            Validation.validatePlaca(current.placa)?.let { put(EntryField.PLACA.key, it) }
            if (type == RecordType.PROVIDER && current.empresa.isBlank()) {
                put(EntryField.EMPRESA.key, "Ingrese la empresa")
            }
            if (type == RecordType.TRUCKER) {
                Validation.validateContainer(current.contenedor)?.let { put(EntryField.CONTENEDOR.key, it) }
                Validation.validateSeal(current.marchamo)?.let { put(EntryField.MARCHAMO.key, it) }
                if (current.despachador.isBlank()) {
                    put(EntryField.DESPACHADOR.key, "Ingrese el despachador")
                }
            }
        }

        if (errors.isNotEmpty()) {
            form.update { it.copy(fieldErrors = errors) }
            return
        }

        viewModelScope.launch {
            form.update { it.copy(saving = true) }

            val uniquenessErrors = buildMap {
                if (current.cedula.isNotBlank() && repository.existsInsideCedula(current.cedula)) {
                    put(EntryField.CEDULA.key, "Ya hay alguien con esa cédula dentro")
                }
                if (type == RecordType.TRUCKER && current.marchamo.isNotBlank() &&
                    repository.existsInsideSeal(current.marchamo)
                ) {
                    put(EntryField.MARCHAMO.key, "Ese marchamo ya está registrado dentro")
                }
            }
            if (uniquenessErrors.isNotEmpty()) {
                form.update {
                    it.copy(
                        saving = false,
                        fieldErrors = current.fieldErrors + uniquenessErrors,
                    )
                }
                return@launch
            }

            val record = NewAccessRecord(
                type = type,
                fullName = current.fullName,
                idNumber = current.cedula.ifBlank { null },
                licensePlate = current.placa.ifBlank { null },
                company = current.empresa.ifBlank { null },
                visitingPerson = current.visitaA.ifBlank { null },
                containerNumber = current.contenedor.ifBlank { null },
                sealNumber = current.marchamo.ifBlank { null },
                dispatcher = current.despachador.ifBlank { null },
                observations = current.observaciones.ifBlank { null },
            )
            val saved = repository.registerEntry(record)
            form.update { it.copy(saving = false, savedRecord = saved) }
        }
    }

    fun reset() {
        form.value = EntryUiState(type = type, settings = form.value.settings)
    }
}

enum class EntryField(val key: String) {
    FULL_NAME("fullName"),
    CEDULA("cedula"),
    PLACA("placa"),
    EMPRESA("empresa"),
    VISITA_A("visitaA"),
    CONTENEDOR("contenedor"),
    MARCHAMO("marchamo"),
    DESPACHADOR("despachador"),
    OBSERVACIONES("observaciones"),
}