package com.shoropio.controlingreso.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoropio.controlingreso.data.preferences.AppSettings
import com.shoropio.controlingreso.data.preferences.SettingsRepository
import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.DateRange
import com.shoropio.controlingreso.domain.model.HistoryFilter
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.domain.repository.AccessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class HistoryUiState(
    val query: String = "",
    val filter: HistoryFilter = HistoryFilter(),
    val records: List<AccessRecord> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AccessRepository,
    private val preferences: SettingsRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(HistoryFilter())

    /**
     * Texto del buscador actualizado al instante, sin esperar a que termine la
     * consulta en la base de datos. Evita que el campo se revierta o salte
     * caracteres mientras la búsqueda está en curso.
     */
    val queryText: StateFlow<String> = query.asStateFlow()

    val state: StateFlow<HistoryUiState> = combine(query, filter, preferences.settings) { q, f, settings ->
            Triple(q, f, settings)
        }
        .flatMapLatest { (q, f, settings) ->
            repository.search(f, q).map { list ->
                HistoryUiState(
                    query = q,
                    filter = f,
                    records = list,
                    settings = settings,
                    isLoading = false,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun selectRange(range: DateRange) {
        if (range != DateRange.CUSTOM) {
            filter.update {
                it.copy(range = range, customStart = null, customEnd = null)
            }
        } else {
            filter.update { it.copy(range = range) }
        }
    }

    fun setCustomRange(start: Long, end: Long) {
        filter.update {
            it.copy(range = DateRange.CUSTOM, customStart = start, customEnd = end)
        }
    }

    fun selectType(type: RecordType?) {
        filter.update { it.copy(type = type) }
    }

    fun selectStatus(status: RecordStatus?) {
        filter.update { it.copy(status = status) }
    }

    fun clearFilters() {
        query.value = ""
        filter.value = HistoryFilter()
    }
}