package dev.syta.myaudioevents.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.syta.myaudioevents.ui.MaeAppState
import dev.syta.myaudioevents.ui.MaeSharedViewModel
import dev.syta.myaudioevents.ui.screens.categories.CategoriesScreen
import dev.syta.myaudioevents.ui.screens.record_list.RecordListScreen
import dev.syta.myaudioevents.ui.screens.recorder.RecorderScreen

@Composable
fun MaeNavHost(
    appState: MaeAppState,
    startDestination: String = Recorder.route,
    sharedViewModel: MaeSharedViewModel
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
    ) {
        composable(Recorder.route) {
            RecorderScreen(
                sharedViewModel = sharedViewModel,
            )
        }
        composable(Recordings.route) {
            RecordListScreen(
                sharedViewModel = sharedViewModel,
            )
        }
        composable(Categories.route) {
            CategoriesScreen(
                sharedViewModel = sharedViewModel,
            )
        }
    }
}