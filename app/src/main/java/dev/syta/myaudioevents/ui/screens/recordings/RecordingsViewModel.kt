package dev.syta.myaudioevents.ui.screens.recordings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.syta.myaudioevents.data.model.AudioRecording
import dev.syta.myaudioevents.data.repository.AudioRecordingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecordingsViewModel @Inject constructor(
    audioRecordingRepository: AudioRecordingRepository
) : ViewModel() {
    private val recordingsFlow = audioRecordingRepository.getAudioRecordings()

    val uiState = recordingsFlow.map { recordings ->
        if (recordings.isEmpty()) {
            RecordingsScreenUiState.Loading
        } else {
            RecordingsScreenUiState.Ready(
                recordingList = recordings,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RecordingsScreenUiState.Loading,
    )

}

sealed interface RecordingsScreenUiState {
    data object Loading : RecordingsScreenUiState
    data class Ready(
        val recordingList: List<AudioRecording>,
    ) : RecordingsScreenUiState
}