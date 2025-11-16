package pe.com.zzynan.euzin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.com.zzynan.euzin.data.local.model.TripWithFuelEntries
import pe.com.zzynan.euzin.data.repository.TripRepository

/**
 * ViewModel para el listado de viajes con búsqueda.
 */
class TripListViewModel(private val tripRepository: TripRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val trips: StateFlow<List<TripWithFuelEntries>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) tripRepository.getTrips() else tripRepository.searchTrips(query)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val query: StateFlow<String> = searchQuery
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun onQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun deleteTrip(trip: TripWithFuelEntries) {
        viewModelScope.launch {
            tripRepository.deleteTrip(trip.trip)
        }
    }

    companion object {
        fun provideFactory(repository: TripRepository): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.CreationExtras): T {
                    @Suppress("UNCHECKED_CAST")
                    return TripListViewModel(repository) as T
                }
            }
    }
}
