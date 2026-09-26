package com.shoropio.controlingreso.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.ui.components.AppTitleBar
import com.shoropio.controlingreso.ui.components.SectionHeader
import com.shoropio.controlingreso.utils.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    type: RecordType,
    onNavigateBack: () -> Unit,
    viewModel: EntryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AppTitleBar(
                title = when (type) {
                    RecordType.VISIT -> "NUEVA VISITA"
                    RecordType.PROVIDER -> "NUEVO PROVEEDOR"
                    RecordType.TRUCKER -> "NUEVO TRAILERO"
                },
                onBackClick = onNavigateBack,
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 110.dp),
                modifier = Modifier.weight(1f),
            ) {
                item { SectionHeader("INFORMACIÓN PERSONAL") }

                item {
                    FormField(
                        value = state.fullName,
                        onValueChange = { viewModel.onFieldChange(EntryField.FULL_NAME, it) },
                        label = "Nombre completo",
                        error = state.fieldErrors[EntryField.FULL_NAME.key],
                    )
                }
                item {
                    FormField(
                        value = state.cedula,
                        onValueChange = { viewModel.onFieldChange(EntryField.CEDULA, it) },
                        label = "Cédula",
                        error = state.fieldErrors[EntryField.CEDULA.key],
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                    )
                    state.knownMatch?.let { match ->
                        KnownMatchCard(
                            matchName = match.fullName,
                            matchDetail = listOfNotNull(match.company, match.licensePlate, match.dispatcher)
                                .joinToString(" · "),
                            onUse = viewModel::useKnownMatch,
                        )
                    }
                }
                if (type == RecordType.PROVIDER) {
                    item {
                        FormField(
                            value = state.empresa,
                            onValueChange = { viewModel.onFieldChange(EntryField.EMPRESA, it) },
                            label = "Empresa",
                            error = state.fieldErrors[EntryField.EMPRESA.key],
                        )
                        if (state.companies.isNotEmpty()) {
                            SuggestionRow(state.companies, viewModel::selectCompany)
                        }
                    }
                }

                item { SectionHeader("VEHÍCULO") }
                item {
                    FormField(
                        value = state.placa,
                        onValueChange = { viewModel.onFieldChange(EntryField.PLACA, it) },
                        label = "Placa",
                        error = state.fieldErrors[EntryField.PLACA.key],
                        capitalizeLetters = true,
                    )
                    if (type == RecordType.TRUCKER) {
                        state.knownPlateMatch?.let { match ->
                            KnownMatchCard(
                                matchName = match.fullName,
                                matchDetail = "Placa ${match.licensePlate.orEmpty()}",
                                onUse = viewModel::useKnownPlateMatch,
                            )
                        }
                    }
                }

                when (type) {
                    RecordType.VISIT, RecordType.PROVIDER -> {
                        item { SectionHeader("VISITA") }
                        item {
                            FormField(
                                value = state.visitaA,
                                onValueChange = { viewModel.onFieldChange(EntryField.VISITA_A, it) },
                                label = "A quién visita",
                                placeholder = "Nombre del colaborador, área o departamento",
                                error = state.fieldErrors[EntryField.VISITA_A.key],
                            )
                            if (state.visitingPersons.isNotEmpty()) {
                                SuggestionRow(state.visitingPersons, viewModel::selectVisitingPerson)
                            }
                        }
                    }
                    RecordType.TRUCKER -> {
                        item { SectionHeader("TRANSPORTE") }
                        item {
                            FormField(
                                value = state.contenedor,
                                onValueChange = { viewModel.onFieldChange(EntryField.CONTENEDOR, it) },
                                label = "Número de contenedor",
                                error = state.fieldErrors[EntryField.CONTENEDOR.key],
                                capitalizeLetters = true,
                            )
                            FormField(
                                value = state.marchamo,
                                onValueChange = { viewModel.onFieldChange(EntryField.MARCHAMO, it) },
                                label = "Número de marchamo",
                                error = state.fieldErrors[EntryField.MARCHAMO.key],
                                capitalizeLetters = true,
                            )
                            FormField(
                                value = state.despachador,
                                onValueChange = { viewModel.onFieldChange(EntryField.DESPACHADOR, it) },
                                label = "Despachador",
                                error = state.fieldErrors[EntryField.DESPACHADOR.key],
                            )
                            if (state.dispatchers.isNotEmpty()) {
                                SuggestionRow(state.dispatchers, viewModel::selectDispatcher)
                            }
                        }
                    }
                }

                item { SectionHeader("OBSERVACIONES") }
                item {
                    FormField(
                        value = state.observaciones,
                        onValueChange = { viewModel.onFieldChange(EntryField.OBSERVACIONES, it) },
                        label = "Observaciones",
                        minLines = 2,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                TextButton(
                    onClick = {
                        viewModel.reset()
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("CANCELAR")
                }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = viewModel::submit,
                    enabled = !state.saving,
                    modifier = Modifier.weight(2f),
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(18.dp).height(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Guardando...")
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "REGISTRAR ENTRADA",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    state.savedRecord?.let { saved ->
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            title = { Text("Entrada registrada") },
            text = {
                Column {
                    Text(saved.fullName, fontWeight = FontWeight.Bold)
                    Text(
                        "Hora de entrada: ${Formatters.formatTime(saved.entryDateTime, state.settings.use24Hour)}",
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.reset()
                    onNavigateBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Listo")
                }
            },
        )
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    error: String? = null,
    minLines: Int = 1,
    capitalizeLetters: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Next,
    ),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        minLines = minLines,
        keyboardOptions = if (capitalizeLetters) {
            keyboardOptions.copy(capitalization = KeyboardCapitalization.Characters)
        } else {
            keyboardOptions
        },
        singleLine = minLines == 1,
    )
}

@Composable
private fun KnownMatchCard(
    matchName: String,
    matchDetail: String,
    onUse: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = "Coincidencia encontrada",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(matchName, fontWeight = FontWeight.Bold)
                Text(matchDetail, style = MaterialTheme.typography.bodySmall)
            }
            FilledTonalButton(onClick = onUse) { Text("Usar datos") }
        }
    }
}

@Composable
private fun SuggestionRow(items: List<String>, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
    ) {
        items(items) { item ->
            FilledTonalButton(onClick = { onSelect(item) }) {
                Text(item, maxLines = 1)
            }
        }
    }
}