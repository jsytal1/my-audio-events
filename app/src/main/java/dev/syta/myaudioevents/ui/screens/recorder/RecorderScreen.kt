package dev.syta.myaudioevents.ui.screens.recorder

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.syta.myaudioevents.R
import dev.syta.myaudioevents.designsystem.MaeBackground
import dev.syta.myaudioevents.ui.DevicePreviews
import dev.syta.myaudioevents.ui.theme.MaeTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecorderScreen(
    modifier: Modifier = Modifier,
    viewModel: RecorderViewModel = RecorderViewModel(),
) {
    val context = LocalContext.current
    lateinit var recordAudioPermissionState: PermissionState

    recordAudioPermissionState =
        rememberPermissionState(permission = android.Manifest.permission.RECORD_AUDIO,
            onPermissionResult = { isGranted ->
                if (isGranted) {
                    viewModel.startRecording()
                } else {
                    if (recordAudioPermissionState.status.shouldShowRationale) {
                        Toast.makeText(
                            context, "Permission request denied.", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Microphone permission is needed.\nPlease enable in Settings.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            RecordButton(
                isRecording = viewModel.isRecording,
                onClick = {
                    if (viewModel.isRecording) {
                        viewModel.stopRecording()
                    } else if (recordAudioPermissionState.status.isGranted) {
                        viewModel.startRecording()
                    } else if (recordAudioPermissionState.status.shouldShowRationale) {
                        viewModel.showPermissionRationale()
                    } else {
                        recordAudioPermissionState.launchPermissionRequest()
                    }
                },
            )
        },
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
            if (viewModel.showPermissionRationale) {
                AlertDialog(
                    onDismissRequest = { viewModel.hidePermissionRationale() },
                    title = { Text("Microphone Permission") },
                    text = { Text("The microphone is required for this feature. Please grant the permission.") },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.hidePermissionRationale() },
                        ) {
                            Text("Dismiss")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                recordAudioPermissionState.launchPermissionRequest()
                                viewModel.hidePermissionRationale()
                            },
                        ) {
                            Text("Grant Permission")
                        }
                    },
                )
            }
        }
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

@Composable
fun RecordButton(
    isRecording: Boolean = false,
    onClick: () -> Unit = {},
) {
    FloatingActionButton(
        onClick = onClick,
    ) {
        Crossfade(
            targetState = isRecording, label = "Recording Toggle"
        ) { isRecording ->
            Icon(
                painter = painterResource(
                    if (isRecording) R.drawable.baseline_stop_24
                    else R.drawable.baseline_mic_24
                ), contentDescription = if (isRecording) stringResource(R.string.stop_recording)
                else stringResource(R.string.start_recording)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRecordButton() {
    MaeTheme {
        RecordButton()
    }
}