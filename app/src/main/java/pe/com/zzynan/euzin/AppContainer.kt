package pe.com.zzynan.euzin

import android.content.Context
import pe.com.zzynan.euzin.data.local.AppDatabase
import pe.com.zzynan.euzin.data.repository.TripRepository
import pe.com.zzynan.euzin.data.repository.TripRepositoryImpl
import pe.com.zzynan.euzin.preferences.DriverPreferences

/**
 * Proveedor simple de dependencias para la app.
 */
class AppContainer(context: Context) {
    private val database: AppDatabase = AppDatabase.getInstance(context)
    val tripRepository: TripRepository = TripRepositoryImpl(database)
    val driverPreferences: DriverPreferences = DriverPreferences(context)
}
