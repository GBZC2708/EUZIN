package pe.com.zzynan.euzin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pe.com.zzynan.euzin.AppContainer
import pe.com.zzynan.euzin.ui.screens.TripFormScreen
import pe.com.zzynan.euzin.ui.screens.TripListScreen
import pe.com.zzynan.euzin.ui.viewmodel.TripFormViewModel
import pe.com.zzynan.euzin.ui.viewmodel.TripListViewModel

sealed class Destinations(val route: String) {
    data object TripList : Destinations("tripList")
    data object TripForm : Destinations("tripForm?tripId={tripId}") {
        fun createRoute(id: Long?) = "tripForm?tripId=${id ?: -1}"
    }
}

@Composable
fun EuzinNavHost(container: AppContainer, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destinations.TripList.route, modifier = modifier) {
        composable(Destinations.TripList.route) {
            val listViewModel: TripListViewModel = viewModel(factory = TripListViewModel.provideFactory(container.tripRepository))
            TripListScreen(
                viewModel = listViewModel,
                onCreateTrip = { navController.navigate(Destinations.TripForm.createRoute(null)) },
                onOpenTrip = { id -> navController.navigate(Destinations.TripForm.createRoute(id)) }
            )
        }
        composable(
            route = Destinations.TripForm.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType; defaultValue = -1 })
        ) { backStackEntry ->
            val savedStateHandle = backStackEntry.savedStateHandle
            val formViewModel: TripFormViewModel = viewModel(
                factory = TripFormViewModel.provideFactory(container.tripRepository, container.driverPreferences, savedStateHandle)
            )
            TripFormScreen(
                viewModel = formViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToTrip = { id ->
                    navController.popBackStack()
                    navController.navigate(Destinations.TripForm.createRoute(id))
                }
            )
        }
    }
}
