package com.shoropio.controlingreso.ui.report

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoropio.controlingreso.data.csv.CsvExporter
import com.shoropio.controlingreso.data.preferences.AppSettings
import com.shoropio.controlingreso.data.preferences.SettingsRepository
import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.domain.model.ReportFilter
import com.shoropio.controlingreso.domain.model.StatsToday
import com.shoropio.controlingreso.domain.repository.AccessRepository
import com.shoropio.controlingreso.utils.Formatters
import com.shoropio.controlingreso.utils.TimeWindow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportUiState(
    val start: Long = TimeWindow.startOfDay(System.currentTimeMillis()),
    val end: Long = System.currentTimeMillis(),
    val type: RecordType? = null,
    val status: RecordStatus? = null,
    val records: List<AccessRecord> = emptyList(),
    val stats: StatsToday = StatsToday(),
    val settings: AppSettings = AppSettings(),
    val generating: Boolean = false,
    val generated: Boolean = false,
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: AccessRepository,
    private val preferences: SettingsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val fields = MutableStateFlow(
        ReportUiState(
            start = TimeWindow.startOfDay(System.currentTimeMillis()),
            end = System.currentTimeMillis(),
        ),
    )

    val state: StateFlow<ReportUiState> = combine(fields, preferences.settings) { ui, settings ->
        ui.copy(settings = settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), fields.value)

    private val _exportRequests = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val exportRequests: SharedFlow<String> = _exportRequests

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages

    private var pendingCsv: String? = null

    init {
        viewModelScope.launch { refreshStats() }
    }

    fun onStartChange(value: Long) {
        fields.update { it.copy(start = value, generated = false) }
    }

    fun onEndChange(value: Long) {
        fields.update { it.copy(end = value, generated = false) }
    }

    fun onTypeChange(value: RecordType?) {
        fields.update { it.copy(type = value, generated = false) }
    }

    fun onStatusChange(value: RecordStatus?) {
        fields.update { it.copy(status = value, generated = false) }
    }

    fun generate() {
        val current = fields.value
        contentLauncher(current)
    }

    private fun contentLauncher(current: ReportUiState) {
        viewModelScope.launch {
            fields.update { it.copy(generating = true) }
            refreshStats()
            val filter = ReportFilter(current.start, current.end, current.type, current.status)
            val records = repository.getReportRecords(filter)
            fields.update {
                it.copy(records = records, generating = false, generated = true)
            }
        }
    }

    fun exportCsv() {
        val current = fields.value
        if (current.records.isEmpty()) return
        pendingCsv = CsvExporter.build(
            current.records,
            current.settings.dateFormat.pattern,
            current.settings.use24Hour,
        )
        val fileName = "reporte_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.csv"
        _exportRequests.tryEmit(fileName)
    }

    fun writeCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                val content = pendingCsv
                if (content == null) {
                    _messages.emit("No hay datos para exportar")
                    return@launch
                }
                context.contentResolver.openOutputStream(uri, "w")?.use { out ->
                    out.write(content.toByteArray(Charsets.UTF_8))
                } ?: error("No se pudo abrir el archivo")
                _messages.emit("Reporte CSV exportado")
            } catch (e: Exception) {
                _messages.emit("No se pudo exportar el reporte")
            }
        }
    }

    private suspend fun refreshStats() {
        val start = TimeWindow.startOfDay(System.currentTimeMillis())
        val end = TimeWindow.endOfDay(System.currentTimeMillis())
        val entries = repository.countEntriesOn(start, end)
        val exits = repository.countExitsOn(start, end)
        val inside = repository.observeInsideCounts()
        val counts = inside.first()
        fields.update {
            it.copy(stats = StatsToday(entries = entries, exits = exits, insideCounts = counts))
        }
    }
}