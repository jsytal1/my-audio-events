package dev.syta.myaudioevents.ui.screens.recorder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.syta.myaudioevents.data.event.Events
import dev.syta.myaudioevents.services.AudioRecorderServiceManager
import dev.syta.myaudioevents.services.AudioRecorderState
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor(
    private val serviceManager: AudioRecorderServiceManager
) : ViewModel() {

    init {
        EventBus.getDefault().register(this)
    }

    override fun onCleared() {
        EventBus.getDefault().unregister(this)
        super.onCleared()
    }

    var recordingStatus by mutableStateOf(AudioRecorderState.IDLE)
        private set

    var showPermissionRationale by mutableStateOf(false)
        private set

    fun showPermissionRationale() {
        showPermissionRationale = true
    }

    fun hidePermissionRationale() {
        showPermissionRationale = false
    }

    fun startRecording() {
        serviceManager.startRecording()
    }

    fun stopRecording() {
        serviceManager.stopRecording()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun gotStatusEvent(event: Events.AudioRecorderState) {
        recordingStatus = event.state
    }
}