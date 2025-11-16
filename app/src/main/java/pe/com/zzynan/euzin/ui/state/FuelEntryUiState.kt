package pe.com.zzynan.euzin.ui.state

/**
 * Estado de una fila de combustible en el formulario.
 */
data class FuelEntryUiState(
    val fuelDate: String = "",
    val gallons: String = "0",
    val pricePerGallon: String = "0",
    val autoCalculatedAmount: Double = 0.0,
    val realPaidAmount: String = "0"
)
