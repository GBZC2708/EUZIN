package pe.com.zzynan.euzin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.com.zzynan.euzin.ui.state.FuelEntryUiState
import pe.com.zzynan.euzin.ui.state.TripFormState
import pe.com.zzynan.euzin.ui.theme.BalanceNegative
import pe.com.zzynan.euzin.ui.theme.BalanceNeutral
import pe.com.zzynan.euzin.ui.theme.BalancePositive
import pe.com.zzynan.euzin.ui.viewmodel.TripFormViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults


/**
 * Pantalla de creación/edición de viaje.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFormScreen(viewModel: TripFormViewModel, onBack: () -> Unit, onNavigateToTrip: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard: ClipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val showDeleteConfirm = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TripFormViewModel.UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is TripFormViewModel.UiEvent.DuplicatedGret -> {
                    snackbarHostState.showSnackbar("GRET repetido, abriendo registro existente")
                    onNavigateToTrip(event.tripId)
                }
                TripFormViewModel.UiEvent.Saved -> snackbarHostState.showSnackbar("Viaje guardado")
                TripFormViewModel.UiEvent.Deleted -> {
                    snackbarHostState.showSnackbar("Viaje eliminado")
                    onBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == null) "Nuevo viaje" else "Editar viaje") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    IconButton(onClick = {
                        val text = viewModel.buildShareText()
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(text))
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Información del viaje copiada. Pégala en WhatsApp u otra aplicación.")
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!state.isReadOnly) {
                IconButton(onClick = { viewModel.saveTrip() }) {
                    Icon(Icons.Default.Save, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DatosConductorCard(state = state, enabled = !state.isReadOnly, onChange = viewModel::updateField) }
            item { ViaticosCard(state = state, enabled = !state.isReadOnly, onChange = viewModel::updateField) }
            item { GastosCard(state = state, enabled = !state.isReadOnly, onChange = viewModel::updateField) }
            item {
                CombustibleCard(
                    fuelEntries = state.fuelEntries,
                    enabled = !state.isReadOnly,
                    onAdd = viewModel::addFuelEntry,
                    onRemove = viewModel::removeFuelEntry,
                    onChange = viewModel::updateFuelEntry,
                    totalAuto = state.totalFuelAutoAmount,
                    totalReal = state.totalFuelRealAmount
                )
            }
            item { TotalesCard(state = state) }
            if (!state.isReadOnly && state.id != null) {
                item {
                    Button(
                        onClick = { showDeleteConfirm.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar viaje", color = Color.White)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Deseas eliminar este viaje?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTrip()
                    showDeleteConfirm.value = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm.value = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun DatosConductorCard(
    state: TripFormState,
    enabled: Boolean,
    onChange: ((TripFormState) -> TripFormState) -> Unit
) {
    CardSection(title = "Datos del viaje") {
        OutlinedTextField(
            value = state.driverName,
            onValueChange = { new -> onChange { current -> current.copy(driverName = new) } },
            label = { Text("Conductor") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.truckPlate,
            onValueChange = { new -> onChange { current -> current.copy(truckPlate = new) } },
            label = { Text("Placa") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.gretNumber,
            onValueChange = { new -> onChange { current -> current.copy(gretNumber = new) } },
            label = { Text("N° GRET") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.dateStart,
                onValueChange = { new -> onChange { current -> current.copy(dateStart = new) } },
                label = { Text("Fecha inicio (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
            )
            OutlinedTextField(
                value = state.dateEnd,
                onValueChange = { new -> onChange { current -> current.copy(dateEnd = new) } },
                label = { Text("Fecha fin") },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Estado: ${state.status}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = state.status == "CERRADO", onCheckedChange = { checked ->
                if (enabled) onChange { it.copy(status = if (checked) "CERRADO" else "ABIERTO", isReadOnly = checked) }
            })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recordar datos de conductor", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = state.rememberDefaults, onCheckedChange = { value -> onChange { it.copy(rememberDefaults = value) } }, enabled = enabled)
        }
    }
}

@Composable
private fun ViaticosCard(
    state: TripFormState,
    enabled: Boolean,
    onChange: ((TripFormState) -> TripFormState) -> Unit
) {
    CardSection(title = "Viáticos") {
        OutlinedTextField(
            value = state.viaticAmount,
            onValueChange = { new -> onChange { current -> current.copy(viaticAmount = new) } },
            label = { Text("Monto de viáticos") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            enabled = enabled
        )
    }
}

@Composable
private fun GastosCard(
    state: TripFormState,
    enabled: Boolean,
    onChange: ((TripFormState) -> TripFormState) -> Unit
) {
    CardSection(title = "Gastos del viaje") {
        val fields: List<Triple<String, String, (String) -> Unit>> = listOf(
            Triple("Carguío", state.loadingCost) { value -> onChange { it.copy(loadingCost = value) } },
            Triple("Descarguío", state.unloadingCost) { value -> onChange { it.copy(unloadingCost = value) } },
            Triple("Toldeo", state.weighingCost) { value -> onChange { it.copy(weighingCost = value) } },
            Triple("Cochera", state.parkingCost) { value -> onChange { it.copy(parkingCost = value) } },
            Triple("Peajes", state.tollsCost) { value -> onChange { it.copy(tollsCost = value) } },
            Triple("Taxi", state.taxiCost) { value -> onChange { it.copy(taxiCost = value) } },
            Triple("Lavado", state.washingCost) { value -> onChange { it.copy(washingCost = value) } },
            Triple("Copias", state.copiesCost) { value -> onChange { it.copy(copiesCost = value) } },
            Triple("Patero", state.helperCost) { value -> onChange { it.copy(helperCost = value) } },
            Triple("Vigilante", state.securityCost) { value -> onChange { it.copy(securityCost = value) } },
            Triple("Otros", state.otherCost) { value -> onChange { it.copy(otherCost = value) } }
        )
        fields.forEach { (label, value, updater) ->
            OutlinedTextField(
                value = value,
                onValueChange = updater,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = enabled
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = state.otherDescription,
            onValueChange = { new -> onChange { it.copy(otherDescription = new) } },
            label = { Text("Descripción de otros") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
    }
}

@Composable
private fun CombustibleCard(
    fuelEntries: List<FuelEntryUiState>,
    enabled: Boolean,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onChange: (Int, (FuelEntryUiState) -> FuelEntryUiState) -> Unit,
    totalAuto: Double,
    totalReal: Double
) {
    CardSection(title = "Combustible") {
        fuelEntries.forEachIndexed { index, entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = entry.fuelDate,
                            onValueChange = { new -> onChange(index) { current -> current.copy(fuelDate = new) } },
                            label = { Text("Fecha") },
                            modifier = Modifier.weight(1f),
                            enabled = enabled,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                        )
                        OutlinedTextField(
                            value = entry.gallons,
                            onValueChange = { new -> onChange(index) { current -> current.copy(gallons = new) } },
                            label = { Text("Galones") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = enabled
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = entry.pricePerGallon,
                            onValueChange = { new -> onChange(index) { current -> current.copy(pricePerGallon = new) } },
                            label = { Text("Precio/galón") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = enabled
                        )
                        OutlinedTextField(
                            value = "S/ ${"%.2f".format(entry.autoCalculatedAmount)}",
                            onValueChange = {},
                            label = { Text("Monto calculado") },
                            modifier = Modifier.weight(1f),
                            enabled = false
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = entry.realPaidAmount,
                            onValueChange = { new -> onChange(index) { current -> current.copy(realPaidAmount = new) } },
                            label = { Text("Monto real pagado") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = enabled
                        )
                        if (enabled) {
                            IconButton(onClick = { onRemove(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (enabled) {
            Button(onClick = onAdd, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar carga de combustible")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Total combustible (cálculo): S/ ${"%.2f".format(totalAuto)}", style = MaterialTheme.typography.bodyMedium)
        Text("Total combustible (real): S/ ${"%.2f".format(totalReal)}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TotalesCard(state: TripFormState) {
    val balanceColor = when (state.balanceType) {
        "A_FAVOR" -> BalancePositive
        "EN_CONTRA" -> BalanceNegative
        else -> BalanceNeutral
    }
    CardSection(title = "Resumen") {
        Text(
            text = "Total gastos: S/ ${"%.2f".format(state.totalExpenses)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (state.balanceType) {
                "A_FAVOR" -> "Saldo a favor del conductor: S/ ${"%.2f".format(state.balance)}"
                "EN_CONTRA" -> "Saldo en contra del conductor: S/ ${"%.2f".format(kotlin.math.abs(state.balance))}"
                else -> "Viáticos cuadrados: S/ 0.00"
            },
            color = balanceColor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
