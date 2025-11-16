package pe.com.zzynan.euzin.data.repository

import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.euzin.data.local.entity.FuelEntryEntity
import pe.com.zzynan.euzin.data.local.entity.TripEntity
import pe.com.zzynan.euzin.data.local.model.TripWithFuelEntries

/**
 * Abstracción de acceso a datos de viajes.
 */
interface TripRepository {
    fun getTrips(): Flow<List<TripWithFuelEntries>>
    fun searchTrips(query: String): Flow<List<TripWithFuelEntries>>
    suspend fun getTripById(id: Long): TripWithFuelEntries?
    suspend fun getTripByGret(gretNumber: String): TripEntity?
    suspend fun insertTripWithFuel(trip: TripEntity, fuelEntries: List<FuelEntryEntity>): Long
    suspend fun updateTripWithFuel(trip: TripEntity, fuelEntries: List<FuelEntryEntity>)
    suspend fun deleteTrip(trip: TripEntity)
}
