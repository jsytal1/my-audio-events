package dev.syta.myaudioevents.ui.screens.record_list

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.syta.myaudioevents.data.model.AudioRecording
import dev.syta.myaudioevents.data.repository.AudioRecordingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordListViewModel @Inject constructor(
    audioRecordingRepository: AudioRecordingRepository
) : ViewModel() {
    private var player = MediaPlayer()
    private val playbackStateFlow = MutableStateFlow(PlaybackState())

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

    val uiState: StateFlow<RecordListScreenUiState> = audioRecordingRepository.getAudioRecordings()
        .combine(playbackStateFlow) { recordList, playbackState ->
            if (recordList.isEmpty()) {
                RecordListScreenUiState.Loading
            } else {
                RecordListScreenUiState.Ready(recordList, playbackState)
            }
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), RecordListScreenUiState.Loading
        )

    fun play(audioRecording: AudioRecording) {
        player.apply {
            reset()
            setDataSource(audioRecording.filePath)
            prepare()
            start()
            setOnCompletionListener {
                stopPlaying()
            }
        }
        updatePlaybackState(isPlaying = true, currentAudio = audioRecording)
        startPositionUpdater()
    }

    private fun startPositionUpdater() {
        val duration = playbackStateFlow.value.activeAudio?.durationMillis?.toLong() ?: 1000L
        val delay = duration / 100
        viewModelScope.launch {
            while (playbackStateFlow.value.isPlaying) {
                val currentPosition = player.currentPosition
                updatePlaybackState(currentPosition = currentPosition)
                delay(delay)
            }
        }
    }

    private fun updatePlaybackState(
        isPlaying: Boolean = playbackStateFlow.value.isPlaying,
        currentAudio: AudioRecording? = playbackStateFlow.value.activeAudio,
        currentPosition: Int = playbackStateFlow.value.currentPosition
    ) {
        playbackStateFlow.value = playbackStateFlow.value.copy(
            isPlaying = isPlaying, activeAudio = currentAudio, currentPosition = currentPosition
        )
    }

    private fun pausePlaying() {
        player.pause()
        updatePlaybackState(isPlaying = false)
    }

    private fun resumePlaying() {
        player.start()
        updatePlaybackState(isPlaying = true)
        startPositionUpdater()
    }

    private fun stopPlaying() {
        player.stop()
        updatePlaybackState(isPlaying = false, currentAudio = null, currentPosition = 0)
    }

    fun togglePlay() {
        if (playbackStateFlow.value.isPlaying) {
            pausePlaying()
        } else {
            resumePlaying()
        }
    }

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val activeAudio: AudioRecording? = null,
        val currentPosition: Int = 0
    )

    sealed interface RecordListScreenUiState {
        data object Loading : RecordListScreenUiState
        data class Ready(
            val recordList: List<AudioRecording>, val playbackState: PlaybackState = PlaybackState()
        ) : RecordListScreenUiState
    }
}