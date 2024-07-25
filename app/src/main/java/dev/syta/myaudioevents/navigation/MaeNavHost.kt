package dev.syta.myaudioevents.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.syta.myaudioevents.ui.MaeAppState
import dev.syta.myaudioevents.ui.screens.home.HomeScreen
import dev.syta.myaudioevents.ui.screens.watch.WatchScreen

@Composable
fun MaeNavHost(
    appState: MaeAppState,
    startDestination: String = Home.route
) {
    val navController = appState.navController
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Home.route) {
            HomeScreen(
                onClickWatch = {
                    navController.navigate(Watch.route)
                }
            )
        }
        composable(Watch.route) {
            WatchScreen()
        }
    }
}