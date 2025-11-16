package pe.com.zzynan.euzin.ui.state

/**
 * Estado integral del formulario de viaje.
 */
data class TripFormState(
    val id: Long? = null,
    val driverName: String = "",
    val truckPlate: String = "",
    val gretNumber: String = "",
    val dateStart: String = "",
    val dateEnd: String = "",
    val status: String = "ABIERTO",
    val viaticAmount: String = "0",
    val loadingCost: String = "0",
    val unloadingCost: String = "0",
    val weighingCost: String = "0",
    val parkingCost: String = "0",
    val tollsCost: String = "0",
    val taxiCost: String = "0",
    val washingCost: String = "0",
    val copiesCost: String = "0",
    val helperCost: String = "0",
    val securityCost: String = "0",
    val otherCost: String = "0",
    val otherDescription: String = "",
    val fuelEntries: List<FuelEntryUiState> = emptyList(),
    val totalExpenses: Double = 0.0,
    val totalFuelAutoAmount: Double = 0.0,
    val totalFuelRealAmount: Double = 0.0,
    val balance: Double = 0.0,
    val balanceType: String = "NEUTRO",
    val rememberDefaults: Boolean = true,
    val isReadOnly: Boolean = false,
    val isLoading: Boolean = false,
    val createdAt: Long? = null
)
