package pe.com.zzynan.euzin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pe.com.zzynan.euzin.data.local.dao.FuelEntryDao
import pe.com.zzynan.euzin.data.local.dao.TripDao
import pe.com.zzynan.euzin.data.local.entity.FuelEntryEntity
import pe.com.zzynan.euzin.data.local.entity.TripEntity

/**
 * Base de datos Room para los viajes y cargas de combustible.
 */
@Database(
    entities = [TripEntity::class, FuelEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun fuelEntryDao(): FuelEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "euzin_viaticos.db"
                ).build().also { INSTANCE = it }
            }
    }
}
