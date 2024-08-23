package dev.syta.myaudioevents.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import dev.syta.myaudioevents.data.event.Events
import org.greenrobot.eventbus.EventBus

private const val LOGGER_TAG = "AUDIO_RECORDER_SERVICE"

enum class AudioRecorderState {
    RECORDING, IDLE
}

@AndroidEntryPoint
class AudioRecorderService : Service() {
    companion object {
        var isRunning = false
    }

    private var audioRecorderState: AudioRecorderState = AudioRecorderState.IDLE

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(LOGGER_TAG, "onStartCommand")
        startRecording()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d(LOGGER_TAG, "onDestroy")
        super.onDestroy()
        stopRecording()
        isRunning = false
    }

    private fun startRecording() {
        isRunning = true
        if (audioRecorderState == AudioRecorderState.RECORDING) {
            Log.d(LOGGER_TAG, "Recording already in progress")
            return
        }
        Log.d(LOGGER_TAG, "Starting recording")
        audioRecorderState = AudioRecorderState.RECORDING
        broadcastStatus()
    }

    private fun stopRecording() {
        audioRecorderState = AudioRecorderState.IDLE
        broadcastStatus()
    }

    private fun broadcastStatus() {
        Log.d(LOGGER_TAG, "Broadcasting status: $audioRecorderState")
        EventBus.getDefault().post(Events.AudioRecorderState(audioRecorderState))
    }
}
