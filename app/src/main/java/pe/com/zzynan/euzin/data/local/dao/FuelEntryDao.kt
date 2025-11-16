package pe.com.zzynan.euzin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pe.com.zzynan.euzin.data.local.entity.FuelEntryEntity

/**
 * DAO para gestionar cargas de combustible.
 */
@Dao
interface FuelEntryDao {

    @Query("SELECT * FROM fuel_entries WHERE tripId = :tripId")
    suspend fun getByTrip(tripId: Long): List<FuelEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<FuelEntryEntity>)

    @Update
    suspend fun updateEntry(entry: FuelEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: FuelEntryEntity)

    @Query("DELETE FROM fuel_entries WHERE tripId = :tripId")
    suspend fun clearForTrip(tripId: Long)
}
