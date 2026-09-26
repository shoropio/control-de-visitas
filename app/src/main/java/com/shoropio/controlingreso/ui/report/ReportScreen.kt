package com.shoropio.controlingreso.ui.report

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.ui.components.AppDatePickerDialog
import com.shoropio.controlingreso.ui.components.AppTitleBar
import com.shoropio.controlingreso.ui.components.EmptyState
import com.shoropio.controlingreso.ui.components.SectionHeader
import com.shoropio.controlingreso.ui.components.StatChip
import com.shoropio.controlingreso.ui.components.typeIcon
import com.shoropio.controlingreso.utils.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }

    val createLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri -> uri?.let { viewModel.writeCsv(it) } }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) {
        viewModel.exportRequests.collect { fileName -> createLauncher.launch(fileName) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                AppTitleBar("Reportes")

                SectionHeader("REPORTE")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = { pickStart = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Desde", style = MaterialTheme.typography.labelSmall)
                            Text(
                                Formatters.formatDate(state.start, state.settings.dateFormat.pattern),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { pickEnd = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Hasta", style = MaterialTheme.typography.labelSmall)
                            Text(
                                Formatters.formatDate(state.end, state.settings.dateFormat.pattern),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                ChipRow(
                    label = "Tipo",
                    options = listOf("Todos") + RecordType.entries.map { it.label },
                    selectedIndex = (RecordType.entries.indexOf(state.type) + 1)
                        .takeIf { state.type != null } ?: 0,
                    onSelect = { index ->
                        viewModel.onTypeChange(if (index == 0) null else RecordType.entries[index - 1])
                    },
                )
                ChipRow(
                    label = "Estado",
                    options = listOf("Todos", "Dentro", "Salieron"),
                    selectedIndex = when (state.status) {
                        null -> 0
                        RecordStatus.INSIDE -> 1
                        RecordStatus.EXITED -> 2
                    },
                    onSelect = { index ->
                        viewModel.onStatusChange(
                            when (index) {
                                1 -> RecordStatus.INSIDE
                                2 -> RecordStatus.EXITED
                                else -> null
                            },
                        )
                    },
                )

                Button(
                    onClick = viewModel::generate,
                    enabled = !state.generating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    if (state.generating) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(20.dp).height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Generando...")
                    } else {
                        Text("GENERAR REPORTE")
                    }
                }

                SectionHeader("HOY")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatChip("Entradas", state.stats.entries, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    StatChip("Salidas", state.stats.exits, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    StatChip("Dentro", state.stats.insideTotal, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatChip(
                        "Visitas",
                        state.stats.insideCounts[RecordType.VISIT] ?: 0,
                        MaterialTheme.colorScheme.primary,
                        Modifier.weight(1f),
                    )
                    StatChip(
                        "Proveedores",
                        state.stats.insideCounts[RecordType.PROVIDER] ?: 0,
                        MaterialTheme.colorScheme.secondary,
                        Modifier.weight(1f),
                    )
                    StatChip(
                        "Traileros",
                        state.stats.insideCounts[RecordType.TRUCKER] ?: 0,
                        MaterialTheme.colorScheme.tertiary,
                        Modifier.weight(1f),
                    )
                }

                if (state.generated) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "${state.records.size} registros en el rango",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (state.records.isEmpty()) {
                                EmptyState("Sin registros en este rango")
                            } else {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = viewModel::exportCsv,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large,
                                ) {
                                    Icon(
                                        Icons.Filled.FileDownload,
                                        contentDescription = null,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("EXPORTAR CSV")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (pickStart) {
        AppDatePickerDialog(
            title = "Desde",
            initial = state.start,
            onConfirm = { viewModel.onStartChange(it); pickStart = false },
            onDismiss = { pickStart = false },
        )
    }
    if (pickEnd) {
        AppDatePickerDialog(
            title = "Hasta",
            initial = state.end,
            onConfirm = { viewModel.onEndChange(it); pickEnd = false },
            onDismiss = { pickEnd = false },
        )
    }
}

@Composable
private fun ChipRow(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Column(Modifier.padding(top = 2.dp, bottom = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(options.size) { index ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    label = { Text(options[index], maxLines = 1) },
                )
            }
        }
    }
}