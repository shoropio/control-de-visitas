package com.shoropio.controlingreso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shoropio.controlingreso.data.preferences.AppSettings
import com.shoropio.controlingreso.data.preferences.SettingsRepository
import com.shoropio.controlingreso.ui.ControlIngresoApp
import com.shoropio.controlingreso.ui.theme.ControlIngresoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferences: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by preferences.settings.collectAsStateWithLifecycle(
                initialValue = AppSettings(),
            )
            ControlIngresoTheme(settings.theme) {
                ControlIngresoApp()
            }
        }
    }
}