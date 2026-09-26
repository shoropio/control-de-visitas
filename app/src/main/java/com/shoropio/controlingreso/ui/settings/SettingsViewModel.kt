package com.shoropio.controlingreso.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoropio.controlingreso.data.backup.BackupManager
import com.shoropio.controlingreso.data.preferences.AppSettings
import com.shoropio.controlingreso.data.preferences.DateFormatPref
import com.shoropio.controlingreso.data.preferences.SettingsRepository
import com.shoropio.controlingreso.data.preferences.ThemePref
import com.shoropio.controlingreso.domain.repository.AccessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: SettingsRepository,
    private val repository: AccessRepository,
    private val backupManager: BackupManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val state: StateFlow<AppSettings> =
        preferences.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages

    private val _exportRequests = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val exportRequests: SharedFlow<String> = _exportRequests

    private val _importRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val importRequests: SharedFlow<Unit> = _importRequests

    fun updateFincaName(value: String) = viewModelScope.launch { preferences.updateFincaName(value) }

    fun updateGateName(value: String) = viewModelScope.launch { preferences.updateGateName(value) }

    fun updateTheme(value: ThemePref) = viewModelScope.launch { preferences.updateTheme(value) }

    fun updateUse24Hour(value: Boolean) = viewModelScope.launch { preferences.updateUse24Hour(value) }

    fun updateDateFormat(value: DateFormatPref) = viewModelScope.launch { preferences.updateDateFormat(value) }

    fun requestExportBackup() {
        val fileName = "respaldo_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.db"
        _exportRequests.tryEmit(fileName)
    }

    fun exportBackup(uri: Uri) = viewModelScope.launch {
        try {
            val out = context.contentResolver.openOutputStream(uri, "w")
            if (out == null) {
                _messages.emit("No se pudo abrir el destino")
            } else {
                backupManager.exportBackup(out)
                _messages.emit("Respaldo exportado")
            }
        } catch (e: Exception) {
            _messages.emit("No se pudo exportar el respaldo")
        }
    }

    fun requestImportBackup() {
        _importRequests.tryEmit(Unit)
    }

    fun importBackup(uri: Uri) = viewModelScope.launch {
        try {
            val input = context.contentResolver.openInputStream(uri)
            if (input == null) {
                _messages.emit("No se pudo leer el respaldo")
            } else {
                backupManager.importBackup(input)
                _messages.emit("Respaldo importado. Cierre y abra la aplicación.")
            }
        } catch (e: Exception) {
            _messages.emit("Respaldo inválido o corrupto")
        }
    }

    fun deleteOldRecords(days: Int) = viewModelScope.launch {
        val before = System.currentTimeMillis() - days * 86_400_000L
        val deleted = repository.deleteOldExited(before)
        _messages.emit("$deleted registros antiguos eliminados")
    }
}