package pe.com.zzynan.euzin.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val DRIVER_PREFS = "driver_prefs"
private val Context.dataStore by preferencesDataStore(name = DRIVER_PREFS)

/**
 * Gestor de preferencias de conductor usando DataStore.
 */
class DriverPreferences(private val context: Context) {

    data class Defaults(val driverName: String, val truckPlate: String)

    private val driverNameKey = stringPreferencesKey("driverNameDefault")
    private val truckPlateKey = stringPreferencesKey("truckPlateDefault")

    val defaults: Flow<Defaults> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            Defaults(
                driverName = prefs[driverNameKey] ?: "",
                truckPlate = prefs[truckPlateKey] ?: ""
            )
        }

    suspend fun saveDefaults(name: String, plate: String) {
        context.dataStore.edit { prefs: Preferences ->
            prefs[driverNameKey] = name
            prefs[truckPlateKey] = plate
        }
    }
}
