package com.shoropio.controlingreso.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.ui.components.ConfirmExitDialog
import com.shoropio.controlingreso.ui.components.EmptyState
import com.shoropio.controlingreso.ui.components.RecordCard
import com.shoropio.controlingreso.ui.components.SectionHeader
import com.shoropio.controlingreso.ui.components.StatChip
import com.shoropio.controlingreso.ui.components.typeIcon
import com.shoropio.controlingreso.ui.navigation.Routes
import com.shoropio.controlingreso.utils.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTypeSelector by remember { mutableStateOf(false) }
    var exitCandidate by remember { mutableStateOf<AccessRecord?>(null) }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var sheetExitCandidate by remember { mutableStateOf<AccessRecord?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item { DashboardHeader(state.settings.fincaName, state.settings.gateName) }

                item { InsideTotalCard(state.insideTotal) }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatChip(
                            "Visitas",
                            state.counts[RecordType.VISIT] ?: 0,
                            colorFor(RecordType.VISIT),
                            Modifier.weight(1f),
                        )
                        StatChip(
                            "Proveedores",
                            state.counts[RecordType.PROVIDER] ?: 0,
                            colorFor(RecordType.PROVIDER),
                            Modifier.weight(1f),
                        )
                        StatChip(
                            "Traileros",
                            state.counts[RecordType.TRUCKER] ?: 0,
                            colorFor(RecordType.TRUCKER),
                            Modifier.weight(1f),
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { showTypeSelector = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            contentPadding = PaddingValues(horizontal = 12.dp),
                        ) {
                            Icon(Icons.Filled.AddCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "REGISTRAR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "Entrada",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { showCheckoutSheet = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            contentPadding = PaddingValues(horizontal = 12.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "REGISTRAR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "Salida",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }

                item {
                    SectionHeader("DENTRO AHORA")
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { Text("Buscar...") },
                        trailingIcon = {
                            if (state.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true,
                    )
                }

                if (state.insideRecords.isEmpty()) {
                    item { EmptyState("No hay personas dentro") }
                } else {
                    item {
                        Text(
                            text = "${state.insideRecords.size} registros",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    items(state.insideRecords, key = { it.id }) { record ->
                        RecordCard(
                            record = record,
                            settings = state.settings,
                            showExitAction = true,
                            onExitClick = {
                                exitCandidate = record
                            },
                            onClick = { onNavigate(Routes.detail(record.id)) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }

    if (showTypeSelector) {
        TypeSelectorDialog(
            onSelect = { type ->
                showTypeSelector = false
                onNavigate(Routes.entry(type))
            },
            onDismiss = { showTypeSelector = false },
        )
    }

    exitCandidate?.let { record ->
        ConfirmExitDialog(
            record = record,
            settings = state.settings,
            onConfirm = { sealNumber ->
                viewModel.registerExit(record, sealNumber)
                exitCandidate = null
            },
            onDismiss = { exitCandidate = null },
        )
    }

    if (showCheckoutSheet) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            onDismissRequest = { showCheckoutSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    text = "Registrar salida",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Text(
                    text = "Seleccione la persona que sale",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(8.dp))
                if (state.insideRecords.isEmpty()) {
                    EmptyState("No hay personas dentro")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                    ) {
                        items(state.insideRecords, key = { it.id }) { record ->
                            RecordCard(
                                record = record,
                                settings = state.settings,
                                showExitAction = true,
                                onExitClick = {
                                    sheetExitCandidate = record
                                },
                                onClick = { sheetExitCandidate = record },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
        sheetExitCandidate?.let { record ->
            ConfirmExitDialog(
                record = record,
                settings = state.settings,
                onConfirm = { sealNumber ->
                    viewModel.registerExit(record, sealNumber)
                    sheetExitCandidate = null
                },
                onDismiss = { sheetExitCandidate = null },
            )
        }
    }
}

@Composable
private fun DashboardHeader(fincaName: String, gateName: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            text = "CONTROL DE INGRESO",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(Formatters.greeting(), style = MaterialTheme.typography.headlineMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "$fincaName · $gateName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        Text(
            text = Formatters.formatDate(System.currentTimeMillis()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun InsideTotalCard(total: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DENTRO",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "Personas en la finca",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
            Text(
                text = total.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TypeSelectorDialog(
    onSelect: (RecordType) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva entrada") },
        text = {
            Column {
                RecordType.entries.forEach { type ->
                    ListItem(
                        headlineContent = { Text(type.label, fontWeight = FontWeight.Bold) },
                        leadingContent = { Icon(typeIcon(type), contentDescription = null) },
                        modifier = Modifier
                            .clickable { onSelect(type) }
                            .fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun colorFor(type: RecordType): Color = when (type) {
    RecordType.VISIT -> MaterialTheme.colorScheme.primary
    RecordType.PROVIDER -> MaterialTheme.colorScheme.secondary
    RecordType.TRUCKER -> MaterialTheme.colorScheme.tertiary
}