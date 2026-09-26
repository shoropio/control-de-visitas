package com.shoropio.controlingreso.ui.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shoropio.controlingreso.domain.model.DateRange
import com.shoropio.controlingreso.domain.model.HistoryFilter
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.ui.components.AppTitleBar
import com.shoropio.controlingreso.ui.components.EmptyState
import com.shoropio.controlingreso.ui.components.RecordCard
import com.shoropio.controlingreso.ui.components.SectionHeader
import com.shoropio.controlingreso.ui.navigation.Routes
import com.shoropio.controlingreso.utils.TimeWindow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigate: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val queryText by viewModel.queryText.collectAsStateWithLifecycle()
    var showCustomRange by remember { mutableStateOf(false) }
    var customStart by remember { mutableStateOf<Long?>(null) }

    Scaffold { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                AppTitleBar("Historial")

                OutlinedTextField(
                    value = queryText,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    placeholder = { Text("Buscar nombre, cédula, placa, empresa, contenedor, marchamo...") },
                    trailingIcon = {
                        if (queryText.isNotBlank()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                )

                FilterChipRow(
                    label = "Fecha",
                    options = DateRange.entries.map { it.label },
                    selectedIndex = DateRange.entries.indexOf(state.filter.range),
                    onSelect = { index ->
                        val range = DateRange.entries[index]
                        if (range == DateRange.CUSTOM) {
                            customStart = null
                            showCustomRange = true
                        } else {
                            viewModel.selectRange(range)
                        }
                    },
                )

                FilterChipRow(
                    label = "Tipo",
                    options = listOf("Todos") + RecordType.entries.map { it.label },
                    selectedIndex = (RecordType.entries.indexOf(state.filter.type) + 1)
                        .takeIf { state.filter.type != null } ?: 0,
                    onSelect = { index ->
                        viewModel.selectType(if (index == 0) null else RecordType.entries[index - 1])
                    },
                )

                FilterChipRow(
                    label = "Estado",
                    options = listOf("Todos", "Dentro", "Salieron"),
                    selectedIndex = when (state.filter.status) {
                        null -> 0
                        RecordStatus.INSIDE -> 1
                        RecordStatus.EXITED -> 2
                    },
                    onSelect = { index ->
                        viewModel.selectStatus(
                            when (index) {
                                1 -> RecordStatus.INSIDE
                                2 -> RecordStatus.EXITED
                                else -> null
                            },
                        )
                    },
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${state.records.size} registros",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f),
                    )
                    if (queryText.isNotBlank() || state.filter != HistoryFilter()) {
                        TextButton(onClick = viewModel::clearFilters) { Text("Limpiar filtros") }
                    }
                }
                HorizontalDivider()

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.records.isEmpty()) {
                    EmptyState("No hay registros con esos filtros")
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(state.records, key = { it.id }) { record ->
                            RecordCard(
                                record = record,
                                settings = state.settings,
                                onClick = { onNavigate(Routes.detail(record.id)) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomRange) {
        CustomRangePicker(
            initialStart = customStart,
            onRangeConfirmed = { start, end -> viewModel.setCustomRange(start, end) },
            onDismiss = {
                customStart = null
                showCustomRange = false
            },
            customStart = customStart,
            setStart = { customStart = it },
        )
    }
}

@Composable
private fun FilterChipRow(
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

@Composable
private fun CustomRangePicker(
    initialStart: Long?,
    onRangeConfirmed: (Long, Long) -> Unit,
    onDismiss: () -> Unit,
    customStart: Long?,
    setStart: (Long?) -> Unit,
) {
    var step by remember { mutableStateOf(if (customStart == null) 0 else 1) }

    if (step == 0) {
        DatePickerDialogCustom(
            title = "Desde",
            initial = customStart,
            onConfirm = { millis ->
                setStart(millis)
                step = 1
            },
            onDismiss = {
                setStart(null)
                onDismiss()
            },
        )
    } else {
        DatePickerDialogCustom(
            title = "Hasta",
            initial = customStart,
            onConfirm = { end ->
                val start = customStart ?: TimeWindow.startOfDay(System.currentTimeMillis())
                onRangeConfirmed(start, end)
                onDismiss()
            },
            onDismiss = {
                setStart(null)
                onDismiss()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogCustom(
    title: String,
    initial: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val initialUtcMillis = initial?.let {
        LocalDate.ofInstant(Instant.ofEpochMilli(it), zone)
            .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }
    val dateState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialUtcMillis,
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    dateState.selectedDateMillis?.let { selected ->
                        val local = Instant.ofEpochMilli(selected)
                            .atZone(ZoneId.of("UTC")).toLocalDate()
                            .atStartOfDay(zone).toInstant().toEpochMilli()
                        onConfirm(local)
                    }
                },
            ) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    ) {
        androidx.compose.material3.DatePicker(state = dateState)
    }
}