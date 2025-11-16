package pe.com.zzynan.euzin.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.euzin.data.local.AppDatabase
import pe.com.zzynan.euzin.data.local.entity.FuelEntryEntity
import pe.com.zzynan.euzin.data.local.entity.TripEntity
import pe.com.zzynan.euzin.data.local.model.TripWithFuelEntries

/**
 * Implementación de repositorio que coordina DAOs de viajes y combustible.
 */
class TripRepositoryImpl(private val database: AppDatabase) : TripRepository {
    private val tripDao = database.tripDao()
    private val fuelDao = database.fuelEntryDao()

    override fun getTrips(): Flow<List<TripWithFuelEntries>> = tripDao.getTrips()

    override fun searchTrips(query: String): Flow<List<TripWithFuelEntries>> = tripDao.searchTrips(query)

    override suspend fun getTripById(id: Long): TripWithFuelEntries? = tripDao.getTripById(id)

    override suspend fun getTripByGret(gretNumber: String): TripEntity? = tripDao.getTripByGret(gretNumber)

    override suspend fun insertTripWithFuel(trip: TripEntity, fuelEntries: List<FuelEntryEntity>): Long {
        var tripId: Long = -1L   // Inicialización obligatoria

        database.withTransaction {
            tripId = tripDao.insertTrip(trip)
            val entries = fuelEntries.map { it.copy(tripId = tripId) }
            fuelDao.insertEntries(entries)
        }
        return tripId
    }


    override suspend fun updateTripWithFuel(trip: TripEntity, fuelEntries: List<FuelEntryEntity>) {
        database.withTransaction {
            tripDao.updateTrip(trip)
            fuelDao.clearForTrip(trip.id)
            val entries = fuelEntries.map { it.copy(tripId = trip.id) }
            fuelDao.insertEntries(entries)
        }
    }

    override suspend fun deleteTrip(trip: TripEntity) {
        database.withTransaction {
            fuelDao.clearForTrip(trip.id)
            tripDao.deleteTrip(trip)
        }
    }
}
