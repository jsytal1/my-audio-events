package dev.syta.myaudioevents.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

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
)