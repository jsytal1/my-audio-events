package dev.syta.myaudioevents.ui.screens.recorder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RecorderViewModel : ViewModel() {
    var isRecording by mutableStateOf(false)
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
        isRecording = true
    }

    fun stopRecording() {
        isRecording = false
    }
}