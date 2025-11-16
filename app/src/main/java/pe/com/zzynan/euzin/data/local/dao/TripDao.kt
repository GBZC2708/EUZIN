package pe.com.zzynan.euzin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pe.com.zzynan.euzin.data.local.entity.TripEntity
import pe.com.zzynan.euzin.data.local.model.TripWithFuelEntries

/**
 * DAO para operaciones sobre viajes.
 */
@Dao
interface TripDao {

    @Transaction
    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    fun getTrips(): Flow<List<TripWithFuelEntries>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Long): TripWithFuelEntries?

    @Transaction
    @Query("SELECT * FROM trips WHERE gretNumber LIKE '%' || :query || '%' OR driverName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchTrips(query: String): Flow<List<TripWithFuelEntries>>

    @Query("SELECT * FROM trips WHERE gretNumber = :gretNumber LIMIT 1")
    suspend fun getTripByGret(gretNumber: String): TripEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)
}
