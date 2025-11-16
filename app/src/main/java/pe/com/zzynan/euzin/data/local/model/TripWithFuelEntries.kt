package pe.com.zzynan.euzin.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import pe.com.zzynan.euzin.data.local.entity.FuelEntryEntity
import pe.com.zzynan.euzin.data.local.entity.TripEntity

/**
 * Modelo de relación de viaje con sus cargas de combustible.
 */
data class TripWithFuelEntries(
    @Embedded val trip: TripEntity,
    @Relation(parentColumn = "id", entityColumn = "tripId")
    val fuelEntries: List<FuelEntryEntity>
)
