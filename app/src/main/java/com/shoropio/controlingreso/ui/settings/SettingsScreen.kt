package com.shoropio.controlingreso.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shoropio.controlingreso.R
import com.shoropio.controlingreso.data.preferences.DateFormatPref
import com.shoropio.controlingreso.data.preferences.ThemePref
import com.shoropio.controlingreso.ui.components.AppTitleBar
import com.shoropio.controlingreso.ui.components.SectionHeader
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showCleanDialog by remember { mutableStateOf(false) }

    // Estado local de los campos de texto: escribir/borrar actualiza la UI de inmediato
    // y se persiste con un pequeño debounce, de modo que el guardado asíncrono de
    // DataStore nunca revierta el texto mientras se edita.
    var fincaNameText by rememberSaveable { mutableStateOf(settings.fincaName) }
    var gateNameText by rememberSaveable { mutableStateOf(settings.gateName) }

    LaunchedEffect(settings.fincaName) {
        if (fincaNameText != settings.fincaName) fincaNameText = settings.fincaName
    }
    LaunchedEffect(settings.gateName) {
        if (gateNameText != settings.gateName) gateNameText = settings.gateName
    }
    LaunchedEffect(fincaNameText) {
        if (fincaNameText != settings.fincaName) {
            delay(400)
            viewModel.updateFincaName(fincaNameText)
        }
    }
    LaunchedEffect(gateNameText) {
        if (gateNameText != settings.gateName) {
            delay(400)
            viewModel.updateGateName(gateNameText)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri -> uri?.let { viewModel.exportBackup(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.importBackup(it) } }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) {
        viewModel.exportRequests.collect { fileName -> exportLauncher.launch(fileName) }
    }
    LaunchedEffect(Unit) {
        viewModel.importRequests.collect { importLauncher.launch(arrayOf("application/octet-stream")) }
    }

    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            AppTitleBar("Configuración")

            SectionHeader("FINCA")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = fincaNameText,
                onValueChange = { fincaNameText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                label = { Text("Nombre de la finca") },
                singleLine = true,
            )
            OutlinedTextField(
                value = gateNameText,
                onValueChange = { gateNameText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                label = { Text("Nombre de la portería") },
                singleLine = true,
            )

            SectionHeader("APARIENCIA")
            Spacer(Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text("Tema") },
                leadingContent = { Icon(Icons.Filled.Palette, contentDescription = null) },
            )
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ThemePref.entries.forEach { option ->
                    FilterChip(
                        selected = settings.theme == option,
                        onClick = { viewModel.updateTheme(option) },
                        label = { Text(option.label) },
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }
            ListItem(
                headlineContent = { Text("Hora de 24 horas") },
                supportingContent = { Text("Ejemplo: 14:30 en vez de 2:30 PM") },
                leadingContent = { Icon(Icons.Filled.AccessTime, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = settings.use24Hour,
                        onCheckedChange = viewModel::updateUse24Hour,
                    )
                },
            )
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    "Formato de fecha",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(Modifier.padding(top = 4.dp)) {
                    DateFormatPref.entries.forEach { option ->
                        FilterChip(
                            selected = settings.dateFormat == option,
                            onClick = { viewModel.updateDateFormat(option) },
                            label = { Text(option.label) },
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                }
            }

            SectionHeader("RESPALDO")
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                OutlinedButton(
                    onClick = viewModel::requestExportBackup,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Exportar respaldo",
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.width(10.dp))
                OutlinedButton(
                    onClick = viewModel::requestImportBackup,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(Icons.Filled.Restore, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Importar respaldo",
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Text(
                text = "El respaldo incluye todos los registros guardados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            SectionHeader("DATOS")
            Spacer(Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text("Limpiar registros antiguos") },
                supportingContent = { Text("Elimina solo registros con salida registrada") },
                leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null) },
                modifier = Modifier.clickable { showCleanDialog = true },
            )

            SectionHeader("INFORMACIÓN")
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Control de Ingreso v$versionName",
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "Aplicación offline para el control de ingreso y salida de visitas, proveedores y traileros.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = context.getString(R.string.copyright_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                )
            }
        }
    }

    if (showCleanDialog) {
        AlertDialog(
            onDismissRequest = { showCleanDialog = false },
            title = { Text("Limpiar registros antiguos") },
            text = { Text("¿Cuántos días atrás desea eliminar los registros que ya salieron?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteOldRecords(7)
                    showCleanDialog = false
                }) { Text("7 días") }
            },
            dismissButton = {
                TextButton(onClick = { showCleanDialog = false }) { Text("Cancelar") }
            },
        )
    }
}