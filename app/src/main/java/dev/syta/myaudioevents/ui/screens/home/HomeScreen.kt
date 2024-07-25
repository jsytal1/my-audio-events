package dev.syta.myaudioevents.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.syta.myaudioevents.ui.DevicePreviews
import dev.syta.myaudioevents.ui.components.MaeBackground
import dev.syta.myaudioevents.ui.theme.MaeTheme


@Composable
fun HomeScreen(onClickWatch: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("HomeScreen:overview")
    ) {
        Button(
            onClick = onClickWatch
        ) {
            Text("Watch")
        }
    }
}

@DevicePreviews
@Composable
fun HomeScreenPreview() {
    MaeTheme {
        MaeBackground {
            HomeScreen(onClickWatch = {})
        }
    }
}