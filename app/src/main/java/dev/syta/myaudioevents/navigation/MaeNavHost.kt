package dev.syta.myaudioevents.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.syta.myaudioevents.ui.MaeAppState
import dev.syta.myaudioevents.ui.screens.recorder.RecorderScreen
import dev.syta.myaudioevents.ui.screens.watch.WatchScreen

@Composable
fun MaeNavHost(
    appState: MaeAppState,
    startDestination: String = Recorder.route
) {
    val navController = appState.navController
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Recorder.route) {
            RecorderScreen()
        }
        composable(Watch.route) {
            WatchScreen()
        }
    }
}