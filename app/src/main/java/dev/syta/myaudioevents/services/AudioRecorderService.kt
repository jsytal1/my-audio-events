package dev.syta.myaudioevents.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.syta.myaudioevents.data.event.Events
import dev.syta.myaudioevents.data.model.AudioRecording
import dev.syta.myaudioevents.data.repository.AudioRecordingRepository
import dev.syta.myaudioevents.utilities.AUDIO_RECORDING_PATH
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val LOGGER_TAG = "AUDIO_RECORDER_SERVICE"

enum class AudioRecorderState {
    RECORDING, IDLE
}

@AndroidEntryPoint
class AudioRecorderService : Service() {
    @ApplicationContext
    @Inject
    lateinit var context: Context

    @Inject
    lateinit var audioRecordingRepository: AudioRecordingRepository

    companion object {
        var isRunning = false
    }

    private lateinit var baseDir: String

    private var audioRecorderState: AudioRecorderState = AudioRecorderState.IDLE
    private var currentRecordingPath: String = ""
    private var recorder: MediaRecorder? = null

    override fun onCreate() {
        super.onCreate()
        baseDir = "${context.getExternalFilesDir(null)}/$AUDIO_RECORDING_PATH"
    }


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
        val recordingsDir = File(baseDir)
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }

        isRunning = true
        if (audioRecorderState == AudioRecorderState.RECORDING) {
            Log.d(LOGGER_TAG, "Recording already in progress")
            return
        }
        currentRecordingPath = "$baseDir/${createFilename()}"
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(currentRecordingPath)
            try {
                prepare()
                start()
                Log.d(LOGGER_TAG, "Starting recording")
                audioRecorderState = AudioRecorderState.RECORDING
            } catch (e: IOException) {
                Log.e(LOGGER_TAG, "Failed to start recording: ${e.message}")
            }
        }
        broadcastStatus()
    }

    private fun setupMediaRecorder(outputFilePath: String) {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFilePath)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOGGER_TAG, "MediaRecorder prepare() failed", e)
            }
        }

    }

    private fun stopRecording() {
        if (audioRecorderState == AudioRecorderState.RECORDING) {
            stopMediaRecorder()
            finalizeRecording(currentRecordingPath, 60000, 1024 * 1024) // Example values
            audioRecorderState = AudioRecorderState.IDLE
            currentRecordingPath = ""
        }
        broadcastStatus()
    }

    private fun stopMediaRecorder() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    private fun finalizeRecording(filePath: String, durationMillis: Int, fileSize: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            saveRecordingToDatabase("New Recording", filePath, durationMillis, fileSize)
        }
    }

    private suspend fun saveRecordingToDatabase(
        name: String, filePath: String, durationMillis: Int, sizeBytes: Int
    ) {
        val timestampMillis = System.currentTimeMillis()
        val newRecording = AudioRecording(
            name = name,
            filePath = filePath,
            timestampMillis = timestampMillis,
            durationMillis = durationMillis,
            sizeBytes = sizeBytes
        )
        audioRecordingRepository.insertAudioRecording(newRecording)
    }


    private fun broadcastStatus() {
        Log.d(LOGGER_TAG, "Broadcasting status: $audioRecorderState")
        EventBus.getDefault().post(Events.AudioRecorderState(audioRecorderState))
    }

    private fun createFilename(): String {
        return "${System.currentTimeMillis()}.wav"
    }
}
