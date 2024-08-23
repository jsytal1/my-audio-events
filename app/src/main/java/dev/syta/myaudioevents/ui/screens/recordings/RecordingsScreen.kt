package dev.syta.myaudioevents.ui.screens.recordings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.syta.myaudioevents.data.model.AudioRecording
import dev.syta.myaudioevents.ui.MaeSharedViewModel

@Composable
fun RecordingsScreen(
    viewModel: RecordingsViewModel = hiltViewModel(),
    sharedViewModel: MaeSharedViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        sharedViewModel.hideFab()
    }

    RecordingsScreenContent(
        uiState = uiState,
    )
}

@Composable
fun RecordingsScreenContent(
    uiState: RecordingsScreenUiState,
) {
    when (val s = uiState) {
        RecordingsScreenUiState.Loading -> {
            Text("Loading")
        }

        is RecordingsScreenUiState.Ready -> {
            RecordingsList(
                recordingList = s.recordingList,
            )
        }
    }
}


@Composable
fun RecordingsList(
    recordingList: List<AudioRecording>,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(recordingList) {
            RecordingCard(
                it
            )
        }
    }
}

@Composable
fun RecordingCard(
    recording: AudioRecording,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),

            ) {
            Text(
                text = recording.name, style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
