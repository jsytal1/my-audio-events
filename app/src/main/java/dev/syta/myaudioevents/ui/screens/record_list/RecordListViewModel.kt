package dev.syta.myaudioevents.ui.screens.record_list

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.syta.myaudioevents.data.model.AudioRecording
import dev.syta.myaudioevents.data.model.Label
import dev.syta.myaudioevents.data.repository.AudioRecordingRepository
import dev.syta.myaudioevents.data.repository.LabelRepository
import kotlinx.coroutines.Dispatchers
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
    private val audioRecordingRepository: AudioRecordingRepository,
    private val labelRepository: LabelRepository,
    @ApplicationContext private val applicationContext: Context

) : ViewModel() {
    private var player = MediaPlayer()
    private val playbackStateFlow = MutableStateFlow(PlaybackState())
    private val _selectedItem = MutableStateFlow<AudioRecording?>(null)
    private val _showDeleteDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            audioRecordingRepository.createFromFiles(
                assets = applicationContext.assets,
                filesDir = applicationContext.filesDir
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

    val uiState: StateFlow<RecordListScreenUiState> = combine(
        audioRecordingRepository.getAudioRecordings(),
        playbackStateFlow,
        _selectedItem,
        _showDeleteDialog,
        labelRepository.getLabels()
    ) { recordList, playbackState, selectedAudio, showDeleteDialog, labelList ->
        if (recordList.isEmpty()) {
            RecordListScreenUiState.Loading
        } else {
            RecordListScreenUiState.Ready(
                recordList,
                playbackState,
                selectedAudio,
                showDeleteDialog,
                labelList
            )
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), RecordListScreenUiState.Loading
    )

    fun addLabelToRecording(audioRecording: AudioRecording, label: Label) {
        Log.d("RecordListViewModel", "addLabelToRecording: $audioRecording, $label")
        viewModelScope.launch(Dispatchers.IO) {
            audioRecordingRepository.addLabelToRecording(
                labelId = label.id,
                audioRecording = audioRecording
            )
        }
    }

    fun removeLabelFromRecording(audioRecording: AudioRecording, label: Label) {
        viewModelScope.launch(Dispatchers.IO) {
            audioRecordingRepository.removeLabelFromRecording(
                labelId = label.id,
                audioRecording = audioRecording
            )
        }
    }

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

    private fun selectAudio(audioRecording: AudioRecording) {
        _selectedItem.value = audioRecording
    }

    fun showDeleteDialog(audioRecording: AudioRecording) {
        selectAudio(audioRecording)
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun deleteSelected() {
        val audioRecording = _selectedItem.value ?: return
        if (playbackStateFlow.value.activeAudio == audioRecording) {
            stopPlaying()
        }
        viewModelScope.launch(Dispatchers.IO) {
            audioRecordingRepository.deleteAudioRecording(audioRecording)
            hideDeleteDialog()
        }
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
            val recordList: List<AudioRecording>,
            val playbackState: PlaybackState = PlaybackState(),
            val selectedAudio: AudioRecording? = null,
            val showDeleteDialog: Boolean = false,
            val labelList: List<Label>,
        ) : RecordListScreenUiState
    }
}