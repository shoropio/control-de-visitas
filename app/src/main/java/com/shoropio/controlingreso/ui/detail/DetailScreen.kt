package com.shoropio.controlingreso.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.ui.components.AppTitleBar
import com.shoropio.controlingreso.ui.components.ConfirmExitDialog
import com.shoropio.controlingreso.ui.components.StatusBadge
import com.shoropio.controlingreso.utils.Formatters

@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.message.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading || state.record == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }

            val record = state.record ?: return@Box

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                AppTitleBar(
                    title = "Detalle de ${record.type.label.lowercase()}",
                    onBackClick = onNavigateBack,
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = record.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f),
                            )
                            StatusBadge(record.status)
                        }
                        Spacer(Modifier.height(12.dp))

                        DetailRow(record.idNumber, "Cédula")
                        DetailRow(record.licensePlate, "Placa")
                        DetailRow(record.company, "Empresa")
                        DetailRow(record.visitingPerson, "Visita a")
                        DetailRow(record.containerNumber, "Contenedor")
                        DetailRow(record.sealNumber, "Marchamo")
                        DetailRow(record.dispatcher, "Despachador")
                        DetailRow(record.observations, "Observaciones")

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "ENTRADA",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            Formatters.formatDateTime(
                                record.entryDateTime,
                                state.settings.dateFormat.pattern,
                                state.settings.use24Hour,
                            ),
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "SALIDA",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        record.exitDateTime?.let { exit ->
                            Text(
                                Formatters.formatDateTime(
                                    exit,
                                    state.settings.dateFormat.pattern,
                                    state.settings.use24Hour,
                                ),
                                fontWeight = FontWeight.Bold,
                            )
                        } ?: Text(
                            "Pendiente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }

                if (record.status == RecordStatus.INSIDE) {
                    Button(
                        onClick = { showExitDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("REGISTRAR SALIDA")
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    val exitRecord = state.record
    if (showExitDialog && exitRecord != null) {
        ConfirmExitDialog(
            record = exitRecord,
            settings = state.settings,
            onConfirm = { sealNumber ->
                viewModel.registerExit(exitRecord, sealNumber)
                showExitDialog = false
            },
            onDismiss = { showExitDialog = false },
        )
    }
}

@Composable
private fun DetailRow(value: String?, label: String) {
    if (value.isNullOrBlank()) return
    Column(Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}