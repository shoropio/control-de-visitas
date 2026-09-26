package com.shoropio.controlingreso.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemePref(val label: String) {
    SYSTEM("Sistema"),
    LIGHT("Claro"),
    DARK("Oscuro"),
}

enum class DateFormatPref(val label: String, val pattern: String) {
    DD_MM_YYYY("dd/MM/yyyy", "dd/MM/yyyy"),
    MM_DD_YYYY("MM/dd/yyyy", "MM/dd/yyyy"),
}

data class AppSettings(
    val fincaName: String = "Finca",
    val gateName: String = "Portería Principal",
    val theme: ThemePref = ThemePref.SYSTEM,
    val use24Hour: Boolean = true,
    val dateFormat: DateFormatPref = DateFormatPref.DD_MM_YYYY,
)

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun updateFincaName(value: String)
    suspend fun updateGateName(value: String)
    suspend fun updateTheme(value: ThemePref)
    suspend fun updateUse24Hour(value: Boolean)
    suspend fun updateDateFormat(value: DateFormatPref)
}

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private object Keys {
        val FINCA_NAME = stringPreferencesKey("finca_name")
        val GATE_NAME = stringPreferencesKey("gate_name")
        val THEME = stringPreferencesKey("theme")
        val USE_24H = booleanPreferencesKey("use_24_hour")
        val DATE_FORMAT = stringPreferencesKey("date_format")
    }

    override val settings: Flow<AppSettings> = context.appDataStore.data.map { prefs ->
        AppSettings(
            fincaName = prefs[Keys.FINCA_NAME] ?: "Finca",
            gateName = prefs[Keys.GATE_NAME] ?: "Portería Principal",
            theme = prefs[Keys.THEME]
                ?.let { runCatching { ThemePref.valueOf(it) }.getOrNull() }
                ?: ThemePref.SYSTEM,
            use24Hour = prefs[Keys.USE_24H] ?: true,
            dateFormat = prefs[Keys.DATE_FORMAT]
                ?.let { runCatching { DateFormatPref.valueOf(it) }.getOrNull() }
                ?: DateFormatPref.DD_MM_YYYY,
        )
    }

    override suspend fun updateFincaName(value: String) {
        context.appDataStore.edit { it[Keys.FINCA_NAME] = value }
    }

    override suspend fun updateGateName(value: String) {
        context.appDataStore.edit { it[Keys.GATE_NAME] = value }
    }

    override suspend fun updateTheme(value: ThemePref) {
        context.appDataStore.edit { it[Keys.THEME] = value.name }
    }

    override suspend fun updateUse24Hour(value: Boolean) {
        context.appDataStore.edit { it[Keys.USE_24H] = value }
    }

    override suspend fun updateDateFormat(value: DateFormatPref) {
        context.appDataStore.edit { it[Keys.DATE_FORMAT] = value.name }
    }
}