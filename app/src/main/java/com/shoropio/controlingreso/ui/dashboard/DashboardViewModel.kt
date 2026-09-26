package com.shoropio.controlingreso.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoropio.controlingreso.data.preferences.AppSettings
import com.shoropio.controlingreso.data.preferences.SettingsRepository
import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.domain.repository.AccessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val insideRecords: List<AccessRecord> = emptyList(),
    val counts: Map<RecordType, Int> = emptyMap(),
    val insideTotal: Int = 0,
    val searchQuery: String = "",
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AccessRepository,
    private val preferences: SettingsRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val state: StateFlow<DashboardUiState> = combine(
        repository.observeInside(),
        repository.observeInsideCounts(),
        preferences.settings,
        searchQuery,
    ) { inside, counts, settings, query ->
        val filtered = if (query.isBlank()) {
            inside
        } else {
            inside.filter { record ->
                record.fullName.contains(query, ignoreCase = true) ||
                    record.licensePlate?.contains(query, ignoreCase = true) == true ||
                    record.company?.contains(query, ignoreCase = true) == true ||
                    record.idNumber?.contains(query, ignoreCase = true) == true ||
                    record.containerNumber?.contains(query, ignoreCase = true) == true ||
                    record.visitingPerson?.contains(query, ignoreCase = true) == true
            }
        }
        DashboardUiState(
            insideRecords = filtered,
            counts = counts,
            insideTotal = counts.values.sum(),
            searchQuery = query,
            settings = settings,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState(isLoading = true))

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages

    fun onSearchChange(value: String) {
        searchQuery.value = value
    }

    fun registerExit(id: Long, name: String) {
        viewModelScope.launch {
            val ok = repository.registerExit(id, null)
            _messages.emit(
                if (ok) "Salida registrada: $name" else "Este registro ya tiene salida registrada",
            )
        }
    }

    fun registerExit(record: AccessRecord, sealNumber: String) {
        viewModelScope.launch {
            val seal = sealNumber.trim()
            val entrySeal = record.sealNumber?.trim().orEmpty()

            if (seal.isEmpty()) {
                _messages.emit("Ingrese el marchamo de salida")
                return@launch
            }
            if (entrySeal.isNotEmpty() && seal.equals(entrySeal, ignoreCase = true)) {
                _messages.emit("El marchamo de salida debe ser distinto al de entrada")
                return@launch
            }
            if (repository.existsInsideSeal(seal, excludeId = record.id)) {
                _messages.emit("Ese marchamo de salida ya está registrado")
                return@launch
            }

            val ok = repository.registerExit(record.id, seal)
            _messages.emit(
                if (ok) "Salida registrada: ${record.fullName}" else "Este registro ya tiene salida registrada",
            )
        }
    }
}