package dev.syta.myaudioevents.ui

import androidx.compose.runtime.Composable
import dev.syta.myaudioevents.navigation.MaeNavHost

@Composable
fun MaeApp(appState: MaeAppState) {
    MaeNavHost(appState = appState)
}

