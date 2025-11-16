package pe.com.zzynan.euzin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.com.zzynan.euzin.data.local.model.TripWithFuelEntries
import pe.com.zzynan.euzin.ui.theme.BalanceNegative
import pe.com.zzynan.euzin.ui.theme.BalanceNeutral
import pe.com.zzynan.euzin.ui.theme.BalancePositive
import pe.com.zzynan.euzin.ui.viewmodel.TripListViewModel

/**
 * Pantalla de listado de viajes con búsqueda.
 */
@Composable
fun TripListScreen(
    viewModel: TripListViewModel,
    onCreateTrip: () -> Unit,
    onOpenTrip: (Long) -> Unit
) {
    val trips by viewModel.trips.collectAsState()
    val query by viewModel.query.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viajes EUZIN") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTrip, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo viaje", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Buscar por GRET o conductor") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(trips) { trip ->
                    TripCard(trip, onOpenTrip)
                }
            }
        }
    }
}

@Composable
private fun TripCard(trip: TripWithFuelEntries, onOpenTrip: (Long) -> Unit) {
    val statusColor = when (trip.trip.balanceType) {
        "A_FAVOR" -> BalancePositive
        "EN_CONTRA" -> BalanceNegative
        else -> BalanceNeutral
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpenTrip(trip.trip.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = "GRET ${trip.trip.gretNumber}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.weight(1f))
                Text(text = trip.trip.status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Conductor: ${trip.trip.driverName}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "Fechas: ${trip.trip.dateStart} - ${trip.trip.dateEnd ?: ""}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Viáticos: S/ ${"%.2f".format(trip.trip.viaticAmount)}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "Gastos: S/ ${"%.2f".format(trip.trip.totalExpenses)}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Saldo: ${trip.trip.balanceType}", color = statusColor, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "Combustible calc: S/ ${"%.2f".format(trip.trip.totalFuelAutoAmount)}", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "Combustible real: S/ ${"%.2f".format(trip.trip.totalFuelRealAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
