package pe.com.zzynan.euzin.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.com.zzynan.euzin.data.local.entity.FuelEntryEntity
import pe.com.zzynan.euzin.data.local.entity.TripEntity
import pe.com.zzynan.euzin.data.repository.TripRepository
import pe.com.zzynan.euzin.preferences.DriverPreferences
import pe.com.zzynan.euzin.ui.state.FuelEntryUiState
import pe.com.zzynan.euzin.ui.state.TripFormState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.abs

/**
 * ViewModel encargado del formulario de viaje, validaciones y cálculos.
 */
class TripFormViewModel(
    private val tripRepository: TripRepository,
    private val driverPreferences: DriverPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val tripId: Long? = savedStateHandle.get<Long>("tripId")?.takeIf { it != -1L }

    private val _state = MutableStateFlow(TripFormState(fuelEntries = listOf(FuelEntryUiState())))
    val state: StateFlow<TripFormState> = _state
        .stateIn(viewModelScope, SharingStarted.Eagerly, _state.value)

    val events = MutableSharedFlow<UiEvent>()

    init {
        viewModelScope.launch {
            _state.emit(_state.value.copy(isLoading = true))
            if (tripId != null) {
                loadTrip(tripId)
            } else {
                loadDefaults()
            }
            recalc()
            _state.emit(_state.value.copy(isLoading = false))
        }
    }

    private suspend fun loadDefaults() {
        val defaults = driverPreferences.defaults.first()
        _state.emit(
            _state.value.copy(
                driverName = defaults.driverName,
                truckPlate = defaults.truckPlate
            )
        )
    }

    private suspend fun loadTrip(id: Long) {
        val trip = tripRepository.getTripById(id) ?: return
        _state.emit(
            TripFormState(
                id = trip.trip.id,
                driverName = trip.trip.driverName,
                truckPlate = trip.trip.truckPlate,
                gretNumber = trip.trip.gretNumber,
                dateStart = trip.trip.dateStart,
                dateEnd = trip.trip.dateEnd ?: "",
                status = trip.trip.status,
                viaticAmount = trip.trip.viaticAmount.toString(),
                loadingCost = trip.trip.loadingCost.toString(),
                unloadingCost = trip.trip.unloadingCost.toString(),
                weighingCost = trip.trip.weighingCost.toString(),
                parkingCost = trip.trip.parkingCost.toString(),
                tollsCost = trip.trip.tollsCost.toString(),
                taxiCost = trip.trip.taxiCost.toString(),
                washingCost = trip.trip.washingCost.toString(),
                copiesCost = trip.trip.copiesCost.toString(),
                helperCost = trip.trip.helperCost.toString(),
                securityCost = trip.trip.securityCost.toString(),
                otherCost = trip.trip.otherCost.toString(),
                otherDescription = trip.trip.otherDescription.orEmpty(),
                fuelEntries = trip.fuelEntries.map {
                    FuelEntryUiState(
                        fuelDate = it.fuelDate.orEmpty(),
                        gallons = it.gallons.toString(),
                        pricePerGallon = it.pricePerGallon.toString(),
                        autoCalculatedAmount = it.autoCalculatedAmount,
                        realPaidAmount = it.realPaidAmount.toString()
                    )
                }.ifEmpty { listOf(FuelEntryUiState()) },
                totalExpenses = trip.trip.totalExpenses,
                totalFuelAutoAmount = trip.trip.totalFuelAutoAmount,
                totalFuelRealAmount = trip.trip.totalFuelRealAmount,
                balance = trip.trip.balance,
                balanceType = trip.trip.balanceType,
                createdAt = trip.trip.createdAt,
                rememberDefaults = false,
                isReadOnly = trip.trip.status == "CERRADO"
            )
        )
    }

    fun updateField(transform: (TripFormState) -> TripFormState) {
        _state.value = transform(_state.value)
        recalc()
    }

    fun updateFuelEntry(index: Int, transform: (FuelEntryUiState) -> FuelEntryUiState) {
        val entries = _state.value.fuelEntries.toMutableList()
        entries[index] = transform(entries[index])
        _state.value = _state.value.copy(fuelEntries = entries)
        recalc()
    }

    fun addFuelEntry() {
        _state.value = _state.value.copy(fuelEntries = _state.value.fuelEntries + FuelEntryUiState())
        recalc()
    }

    fun removeFuelEntry(index: Int) {
        val entries = _state.value.fuelEntries.toMutableList()
        if (entries.size > 1) {
            entries.removeAt(index)
            _state.value = _state.value.copy(fuelEntries = entries)
            recalc()
        }
    }

    private fun String.asPositiveDouble(): Double = toDoubleOrNull()?.takeIf { it >= 0 } ?: 0.0

    private fun recalc() {
        val s = _state.value
        val loading = s.loadingCost.asPositiveDouble()
        val unloading = s.unloadingCost.asPositiveDouble()
        val weighing = s.weighingCost.asPositiveDouble()
        val parking = s.parkingCost.asPositiveDouble()
        val tolls = s.tollsCost.asPositiveDouble()
        val taxi = s.taxiCost.asPositiveDouble()
        val washing = s.washingCost.asPositiveDouble()
        val copies = s.copiesCost.asPositiveDouble()
        val helper = s.helperCost.asPositiveDouble()
        val security = s.securityCost.asPositiveDouble()
        val other = s.otherCost.asPositiveDouble()
        val fuelCalculated = s.fuelEntries.sumOf { entry ->
            val gallons = entry.gallons.asPositiveDouble()
            val price = entry.pricePerGallon.asPositiveDouble()
            gallons * price
        }
        val totalFuelReal = s.fuelEntries.sumOf { entry -> entry.realPaidAmount.asPositiveDouble() }
        val totalExpenses = loading + unloading + weighing + parking + tolls + taxi + washing + copies + helper + security + other + totalFuelReal
        val viatic = s.viaticAmount.asPositiveDouble()
        val balance = viatic - totalExpenses
        val balanceType = when {
            balance > 0 -> "A_FAVOR"
            balance < 0 -> "EN_CONTRA"
            else -> "NEUTRO"
        }
        _state.value = s.copy(
            totalExpenses = totalExpenses,
            totalFuelAutoAmount = fuelCalculated,
            totalFuelRealAmount = totalFuelReal,
            balance = balance,
            balanceType = balanceType,
            fuelEntries = s.fuelEntries.map {
                val gallons = it.gallons.asPositiveDouble()
                val price = it.pricePerGallon.asPositiveDouble()
                it.copy(autoCalculatedAmount = gallons * price)
            }
        )
    }

    private fun validateDates(): Boolean {
        val start = _state.value.dateStart
        if (start.isBlank()) return false
        return try {
            val startDate = LocalDate.parse(start, formatter)
            if (_state.value.dateEnd.isBlank()) return true
            val endDate = LocalDate.parse(_state.value.dateEnd, formatter)
            !endDate.isBefore(startDate)
        } catch (_: DateTimeParseException) {
            false
        }
    }

    private fun hasNegativeValues(): Boolean {
        val s = _state.value
        val costs = listOf(
            s.viaticAmount,
            s.loadingCost,
            s.unloadingCost,
            s.weighingCost,
            s.parkingCost,
            s.tollsCost,
            s.taxiCost,
            s.washingCost,
            s.copiesCost,
            s.helperCost,
            s.securityCost,
            s.otherCost
        )
        if (costs.any { it.toDoubleOrNull()?.let { value -> value < 0 } == true }) return true
        return s.fuelEntries.any {
            it.gallons.toDoubleOrNull()?.let { v -> v < 0 } == true ||
                it.pricePerGallon.toDoubleOrNull()?.let { v -> v < 0 } == true ||
                it.realPaidAmount.toDoubleOrNull()?.let { v -> v < 0 } == true
        }
    }

    fun saveTrip() {
        viewModelScope.launch {
            val current = _state.value
            if (current.driverName.isBlank() || current.truckPlate.isBlank() || current.gretNumber.isBlank() || current.dateStart.isBlank()) {
                events.emit(UiEvent.ShowMessage("Completa los datos obligatorios"))
                return@launch
            }
            if (!validateDates()) {
                events.emit(UiEvent.ShowMessage("Verifica las fechas (formato YYYY-MM-DD)"))
                return@launch
            }
            if (hasNegativeValues()) {
                events.emit(UiEvent.ShowMessage("No se permiten montos negativos"))
                return@launch
            }
            val duplicated = tripRepository.getTripByGret(current.gretNumber)
            if (duplicated != null && duplicated.id != current.id) {
                events.emit(UiEvent.DuplicatedGret(duplicated.id))
                return@launch
            }
            val fuelEntities = current.fuelEntries.map {
                FuelEntryEntity(
                    tripId = current.id ?: 0,
                    fuelDate = it.fuelDate.ifBlank { null },
                    gallons = it.gallons.asPositiveDouble(),
                    pricePerGallon = it.pricePerGallon.asPositiveDouble(),
                    autoCalculatedAmount = it.autoCalculatedAmount,
                    realPaidAmount = it.realPaidAmount.asPositiveDouble()
                )
            }
            val tripEntity = TripEntity(
                id = current.id ?: 0,
                gretNumber = current.gretNumber,
                driverName = current.driverName,
                truckPlate = current.truckPlate,
                dateStart = current.dateStart,
                dateEnd = current.dateEnd.ifBlank { null },
                viaticAmount = current.viaticAmount.asPositiveDouble(),
                loadingCost = current.loadingCost.asPositiveDouble(),
                unloadingCost = current.unloadingCost.asPositiveDouble(),
                weighingCost = current.weighingCost.asPositiveDouble(),
                parkingCost = current.parkingCost.asPositiveDouble(),
                tollsCost = current.tollsCost.asPositiveDouble(),
                taxiCost = current.taxiCost.asPositiveDouble(),
                washingCost = current.washingCost.asPositiveDouble(),
                copiesCost = current.copiesCost.asPositiveDouble(),
                helperCost = current.helperCost.asPositiveDouble(),
                securityCost = current.securityCost.asPositiveDouble(),
                otherCost = current.otherCost.asPositiveDouble(),
                otherDescription = current.otherDescription.ifBlank { null },
                totalExpenses = current.totalExpenses,
                totalFuelAutoAmount = current.totalFuelAutoAmount,
                totalFuelRealAmount = current.totalFuelRealAmount,
                balance = current.balance,
                balanceType = current.balanceType,
                status = current.status,
                createdAt = current.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            if (current.id == null) {
                val newId = tripRepository.insertTripWithFuel(tripEntity, fuelEntities)
                _state.emit(current.copy(id = newId, createdAt = tripEntity.createdAt))
            } else {
                tripRepository.updateTripWithFuel(tripEntity, fuelEntities)
            }
            if (current.rememberDefaults) {
                driverPreferences.saveDefaults(current.driverName, current.truckPlate)
            }
            events.emit(UiEvent.Saved)
        }
    }

    fun deleteTrip() {
        viewModelScope.launch {
            val id = _state.value.id ?: return@launch
            tripRepository.getTripById(id)?.trip?.let { tripRepository.deleteTrip(it) }
            events.emit(UiEvent.Deleted)
        }
    }

    fun buildShareText(): String {
        val s = _state.value
        val balanceLabel = when (s.balanceType) {
            "A_FAVOR" -> "A favor"
            "EN_CONTRA" -> "En contra"
            else -> "Neutro"
        }
        val fuelLines = s.fuelEntries.joinToString("\n") {
            "- Fecha: ${it.fuelDate.ifBlank { "N/D" }} | Galones: ${it.gallons} | Precio/galón: S/ ${it.pricePerGallon} | Monto calculado: S/ ${"%.2f".format(it.autoCalculatedAmount)} | Monto real: S/ ${it.realPaidAmount}"
        }
        return buildString {
            appendLine("EUZIN INTERNACIONAL S.A.C. – Detalle de viaje")
            appendLine()
            appendLine("Conductor: ${s.driverName}")
            appendLine("Placa: ${s.truckPlate}")
            appendLine("GRET: ${s.gretNumber}")
            appendLine("Fecha inicio: ${s.dateStart}")
            appendLine("Fecha fin: ${s.dateEnd.ifBlank { "N/D" }}")
            appendLine()
            appendLine("Viáticos entregados: S/ ${s.viaticAmount}")
            appendLine()
            appendLine("Gastos:")
            appendLine("- Carguío: S/ ${s.loadingCost}")
            appendLine("- Descarguío: S/ ${s.unloadingCost}")
            appendLine("- Toldeo: S/ ${s.weighingCost}")
            appendLine("- Cochera: S/ ${s.parkingCost}")
            appendLine("- Peajes: S/ ${s.tollsCost}")
            appendLine("- Taxi: S/ ${s.taxiCost}")
            appendLine("- Lavado: S/ ${s.washingCost}")
            appendLine("- Copias: S/ ${s.copiesCost}")
            appendLine("- Patero: S/ ${s.helperCost}")
            appendLine("- Vigilante: S/ ${s.securityCost}")
            appendLine("- Otros: S/ ${s.otherCost} (${s.otherDescription.ifBlank { "Sin detalle" }})")
            appendLine()
            appendLine("Total gastos: S/ ${"%.2f".format(s.totalExpenses)}")
            appendLine()
            appendLine("Combustible:")
            appendLine(fuelLines)
            appendLine()
            appendLine("Total combustible (cálculo): S/ ${"%.2f".format(s.totalFuelAutoAmount)}")
            appendLine("Total combustible (real): S/ ${"%.2f".format(s.totalFuelRealAmount)}")
            appendLine()
            appendLine("Saldo de viáticos: $balanceLabel S/ ${"%.2f".format(abs(s.balance))}")
            appendLine()
            appendLine("Enviado desde la app de viáticos EUZIN.")
        }
    }

    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
        data class DuplicatedGret(val tripId: Long) : UiEvent()
        object Saved : UiEvent()
        object Deleted : UiEvent()
    }

    companion object {
        fun provideFactory(
            repository: TripRepository,
            preferences: DriverPreferences,
            savedStateHandle: SavedStateHandle
        ): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return TripFormViewModel(repository, preferences, savedStateHandle) as T
                }
            }
    }

}
