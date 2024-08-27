package dev.syta.myaudioevents.ui.screens.record_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.syta.myaudioevents.R
import dev.syta.myaudioevents.data.model.AudioRecording
import dev.syta.myaudioevents.designsystem.icon.MaeIcons
import dev.syta.myaudioevents.ui.MaeSharedViewModel
import dev.syta.myaudioevents.ui.screens.label_list.ConfirmDialog
import dev.syta.myaudioevents.utilities.formatDuration
import dev.syta.myaudioevents.utilities.formatMillisToReadableDate
import java.util.Locale

@Composable
fun RecordListScreen(
    viewModel: RecordListViewModel = hiltViewModel(),
    sharedViewModel: MaeSharedViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        sharedViewModel.hideFab()
    }

    RecordListScreenContent(
        uiState = uiState,
        viewModel = viewModel,
    )
}

@Composable
fun RecordListScreenContent(
    uiState: RecordListViewModel.RecordListScreenUiState,
    viewModel: RecordListViewModel,
) {
    when (uiState) {
        RecordListViewModel.RecordListScreenUiState.Loading -> {
            Text("Loading")
        }

        is RecordListViewModel.RecordListScreenUiState.Ready -> {
            RecordList(
                recordingList = uiState.recordList,
                playbackState = uiState.playbackState,
                viewModel = viewModel,
            )
            if (uiState.showDeleteDialog) {
                ConfirmDialog(
                    title = stringResource(R.string.delete),
                    message = { Text(stringResource(R.string.confirm_delete_item)) },
                    onDismiss = viewModel::hideDeleteDialog,
                    onConfirm = {
                        viewModel.deleteSelected()
                        viewModel.hideDeleteDialog() n
                    }
                )
            }
        }
    }
}

@Composable
fun DeleteDialog(onDismiss: () -> Unit, onDelete: () -> Unit) {

}


@Composable
fun RecordList(
    recordingList: List<AudioRecording>,
    playbackState: RecordListViewModel.PlaybackState,
    viewModel: RecordListViewModel,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(recordingList) { recording ->
            val isActive = playbackState.activeAudio == recording
            RecordingCard(
                recording = recording,
                isActive = isActive,
                isPlaying = isActive && playbackState.isPlaying,
                onPlay = { viewModel.play(recording) },
                togglePlay = { if (isActive) viewModel.togglePlay() },
                showDeleteDialog = { viewModel.showDeleteDialog(recording) },
                progress = {
                    if (recording.durationMillis == 0) 0f else
                        playbackState.currentPosition.toFloat() / recording.durationMillis
                }
            )
        }
    }
}

@Composable
fun RecordingCard(
    recording: AudioRecording,
    isActive: Boolean,
    isPlaying: Boolean,
    progress: () -> Float,
    onPlay: () -> Unit,
    togglePlay: () -> Unit,
    showDeleteDialog: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = recording.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {

                        Text(
                            text = formatDuration(recording.durationMillis),
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            text = formatSize(recording.sizeBytes),
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            text = formatMillisToReadableDate(
                                recording.timestampMillis,
                                "MMM dd, HH:mm:ss"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                        )

                    }
                }
                IconButton(
                    onClick = {
                        when {
                            isActive -> togglePlay()
                            else -> onPlay()
                        }
                    }
                ) {
                    Icon(
                        imageVector = when {
                            isPlaying -> MaeIcons.Pause
                            else -> MaeIcons.Play
                        },
                        contentDescription = "Play/Pause",
                    )
                }
                MoreMenu(
                    showDeleteDialog = showDeleteDialog,
                )
            }
            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Composable
fun MoreMenu(showDeleteDialog: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = MaeIcons.MoreVert,
                contentDescription = "More"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    expanded = false
                    showDeleteDialog()
                }
            )
        }
    }

}


fun formatSize(sizeBytes: Int): String {
    val sizeKB = sizeBytes / 1024.0
    val sizeMB = sizeKB / 1024
    val sizeGB = sizeMB / 1024

    return when {
        sizeGB >= 1 -> String.format(Locale.getDefault(), "%.1f GB", sizeGB)
        sizeMB >= 1 -> String.format(Locale.getDefault(), "%.1f MB", sizeMB)
        sizeKB >= 1 -> String.format(Locale.getDefault(), "%.1f KB", sizeKB)
        else -> "$sizeBytes B"
    }
}

@Preview
@Composable
fun ActiveRecordingCardPreview() {
    RecordingCard(
        recording = AudioRecording(
            id = 0,
            name = "Recording 1",
            filePath = "",
            timestampMillis = 0,
            durationMillis = 10000,
            sizeBytes = 1000,
        ),
        isActive = true,
        isPlaying = true,
        progress = { 0.5f },
        onPlay = {},
        togglePlay = {},
        showDeleteDialog = {}
    )
}


@Preview
@Composable
fun InactiveRecordingCardPreview() {
    RecordingCard(
        recording = AudioRecording(
            id = 0,
            name = "Recording 1",
            filePath = "",
            timestampMillis = 0,
            durationMillis = 10000,
            sizeBytes = 1000,
        ),
        isActive = false,
        isPlaying = false,
        progress = { 0.5f },
        onPlay = {},
        togglePlay = {},
        showDeleteDialog = {},
    )
}