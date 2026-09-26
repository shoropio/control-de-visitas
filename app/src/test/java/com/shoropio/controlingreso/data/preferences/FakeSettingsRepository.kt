package com.shoropio.controlingreso.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    private val flow = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = flow
    override suspend fun updateFincaName(value: String) {}
    override suspend fun updateGateName(value: String) {}
    override suspend fun updateTheme(value: ThemePref) {}
    override suspend fun updateUse24Hour(value: Boolean) {}
    override suspend fun updateDateFormat(value: DateFormatPref) {}
}