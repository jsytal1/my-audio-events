package dev.syta.myaudioevents.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import dev.syta.myaudioevents.designsystem.icon.MaeIcons
import dev.syta.myaudioevents.navigation.MaeNavHost

@Composable
fun MaeApp(
    appState: MaeAppState,
    sharedViewModel: MaeSharedViewModel = hiltViewModel()
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                appState.topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(destination.labelId)) },
                        selected = appState.currentDestination?.route == destination.route,
                        onClick = {
                            appState.navigateToTopLevelDestination(destination)
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            val fabState by sharedViewModel.fabState.observeAsState()
            if (fabState?.isVisible == true) {
                FloatingActionButton(
                    onClick = { fabState?.onClick?.invoke() }
                ) {
                    val contentDescription = fabState?.contentDescriptionId?.let { id ->
                        stringResource(id)
                    }

                    Icon(
                        imageVector = fabState?.icon ?: MaeIcons.Add,
                        contentDescription = contentDescription ?: "Action"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {

            MaeNavHost(
                appState = appState,
                sharedViewModel = sharedViewModel
            )
        }
    }
}

