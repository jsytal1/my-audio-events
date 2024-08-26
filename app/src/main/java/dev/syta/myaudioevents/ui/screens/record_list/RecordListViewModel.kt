package dev.syta.myaudioevents.ui.screens.record_list

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
class RecordListViewModel @Inject constructor(
    audioRecordingRepository: AudioRecordingRepository
) : ViewModel() {
    private val recordListFlow = audioRecordingRepository.getAudioRecordings()

    val uiState = recordListFlow.map { recordList ->
        if (recordList.isEmpty()) {
            RecordListScreenUiState.Loading
        } else {
            RecordListScreenUiState.Ready(
                recordList = recordList,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RecordListScreenUiState.Loading,
    )

}

sealed interface RecordListScreenUiState {
    data object Loading : RecordListScreenUiState
    data class Ready(
        val recordList: List<AudioRecording>,
    ) : RecordListScreenUiState
}