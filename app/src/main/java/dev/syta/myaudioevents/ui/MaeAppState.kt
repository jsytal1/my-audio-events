package dev.syta.myaudioevents.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.syta.myaudioevents.navigation.MaeTopLevelDestination
import dev.syta.myaudioevents.navigation.TopLevelDestinations

@Composable
fun rememberMaeAppState(
    navController: NavHostController = rememberNavController()
): MaeAppState {
    return remember(
        navController
    ) {
        MaeAppState(
            navController = navController
        )
    }
}

class MaeAppState(
    val navController: NavHostController
) {

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val topLevelDestinations: List<MaeTopLevelDestination> = TopLevelDestinations

    fun navigateToTopLevelDestination(topLevelDestination: MaeTopLevelDestination) {
        navController.navigate(
            topLevelDestination.route
        ) {
            // Avoid multiple copies of the same destination when reselecting the same item
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }
    }
}