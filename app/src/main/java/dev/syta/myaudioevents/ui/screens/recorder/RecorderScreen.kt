package dev.syta.myaudioevents.ui.screens.recorder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.syta.myaudioevents.designsystem.MaeBackground
import dev.syta.myaudioevents.ui.DevicePreviews
import dev.syta.myaudioevents.ui.theme.MaeTheme


@Composable
fun RecorderScreen() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("RecorderScreen:overview")
    ) {
    }
}

@DevicePreviews
@Composable
fun RecorderScreenPreview() {
    MaeTheme {
        MaeBackground {
            RecorderScreen()
        }
    }
}