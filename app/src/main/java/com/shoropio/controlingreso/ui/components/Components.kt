package com.shoropio.controlingreso.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.controlingreso.data.preferences.AppSettings
import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.utils.Formatters
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun typeIcon(type: RecordType): ImageVector = when (type) {
    RecordType.VISIT -> Icons.Filled.Person
    RecordType.PROVIDER -> Icons.Filled.Business
    RecordType.TRUCKER -> Icons.Filled.LocalShipping
}

@Composable
fun typeColor(type: RecordType): Color = when (type) {
    RecordType.VISIT -> MaterialTheme.colorScheme.primary
    RecordType.PROVIDER -> MaterialTheme.colorScheme.secondary
    RecordType.TRUCKER -> MaterialTheme.colorScheme.tertiary
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTitleBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
fun StatChip(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun RecordCard(
    record: AccessRecord,
    settings: AppSettings,
    showExitAction: Boolean = false,
    onExitClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(typeColor(record.type).copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = typeIcon(record.type),
                    contentDescription = record.type.label,
                    tint = typeColor(record.type),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypeBadge(record.type)
                    if (!record.licensePlate.isNullOrBlank()) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Placa ${record.licensePlate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (record.secondaryLine.isNotBlank()) {
                    Text(
                        text = record.secondaryLine,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        text = "Entrada ${Formatters.formatTime(record.entryDateTime, settings.use24Hour)}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    record.exitDateTime?.let { exit ->
                        Text(
                            text = "Salida ${Formatters.formatTime(exit, settings.use24Hour)}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (showExitAction && onExitClick != null) {
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onExitClick,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Salida", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TypeBadge(type: RecordType) {
    Text(
        text = type.label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = typeColor(type),
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(typeColor(type).copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun StatusBadge(status: RecordStatus) {
    val color = when (status) {
        RecordStatus.INSIDE -> MaterialTheme.colorScheme.primary
        RecordStatus.EXITED -> MaterialTheme.colorScheme.outline
    }
    Text(
        text = status.label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun EmptyState(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(44.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
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
        Text(title, style = MaterialTheme.typography.titleLarge)
        androidx.compose.material3.DatePicker(state = dateState)
    }
}

@Composable
fun ConfirmExitDialog(
    record: AccessRecord,
    settings: AppSettings,
    onConfirm: (sealNumber: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var marchamo by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text("¿Registrar salida?") },
        text = {
            Column {
                Text(record.fullName, fontWeight = FontWeight.Bold)
                Text(
                    "Entrada: ${Formatters.formatTime(record.entryDateTime, settings.use24Hour)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!record.licensePlate.isNullOrBlank()) {
                    Text("Placa: ${record.licensePlate}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = marchamo,
                    onValueChange = { marchamo = it },
                    label = { Text("Marchamo de salida") },
                    placeholder = { Text("Distinto al de entrada") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(marchamo) },
                enabled = marchamo.isNotBlank(),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Registrar salida")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}