package com.shoropio.controlingreso.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoropio.controlingreso.data.preferences.AppSettings
import com.shoropio.controlingreso.data.preferences.SettingsRepository
import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.repository.AccessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class DetailUiState(
    val record: AccessRecord? = null,
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val exiting: Boolean = false,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: AccessRepository,
    private val preferences: SettingsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val id: Long = savedStateHandle["id"] ?: 0L

    private val _exiting = MutableSharedFlow<String>()

    val message: SharedFlow<String> = _exiting

    val state: StateFlow<DetailUiState> = combine(repository.observeById(id), preferences.settings) { record, settings ->
        DetailUiState(record = record, settings = settings, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun registerExit(record: AccessRecord, sealNumber: String? = null) {
        viewModelScope.launch {
            val seal = sealNumber?.trim().orEmpty()
            val entrySeal = record.sealNumber?.trim().orEmpty()

            if (seal.isEmpty()) {
                _exiting.emit("Ingrese el marchamo de salida")
                return@launch
            }
            if (entrySeal.isNotEmpty() && seal.equals(entrySeal, ignoreCase = true)) {
                _exiting.emit("El marchamo de salida debe ser distinto al de entrada")
                return@launch
            }
            if (repository.existsInsideSeal(seal, excludeId = record.id)) {
                _exiting.emit("Ese marchamo de salida ya está registrado")
                return@launch
            }

            val ok = repository.registerExit(record.id, seal)
            _exiting.emit(if (ok) "Salida registrada" else "Este registro ya tiene salida")
        }
    }
}