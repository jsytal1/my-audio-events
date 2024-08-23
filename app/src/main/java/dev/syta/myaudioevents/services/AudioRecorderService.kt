package dev.syta.myaudioevents.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.syta.myaudioevents.MainActivity
import dev.syta.myaudioevents.R
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

const val FOREGROUND_NOTIFICATION_ID = 1293478424
const val CHANNEL_ID = "AudioRecorderService"

const val ACTION_STOP_RECORDING = "ACTION_STOP_RECORDING"

@AndroidEntryPoint
class AudioRecorderService : Service() {
    @ApplicationContext
    @Inject
    lateinit var context: Context

    @Inject
    lateinit var audioRecordingRepository: AudioRecordingRepository

    companion object {
        var isRunning = false

        const val STOP_RECORDING_REQUEST_CODE = 1
        const val OPEN_APP_REQUEST_CODE = 2
    }

    private lateinit var baseDir: String

    private var audioRecorderState: AudioRecorderState = AudioRecorderState.IDLE
    private var currentRecordingPath: String = ""
    private var recorder: MediaRecorder? = null

    override fun onCreate() {
        super.onCreate()
        baseDir = "${context.getExternalFilesDir(null)}/$AUDIO_RECORDING_PATH"

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val label = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID, label, importance)
            notificationChannel.setSound(null, null)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(LOGGER_TAG, "onStartCommand")
        when (intent.action) {
            ACTION_STOP_RECORDING -> stopRecording()
            else -> startRecording()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d(LOGGER_TAG, "onDestroy")
        super.onDestroy()
        stopRecording()
        stopSelf()
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
                startForeground(FOREGROUND_NOTIFICATION_ID, showNotification())
            } catch (e: IOException) {
                Log.e(LOGGER_TAG, "Failed to start recording: ${e.message}")
            }
        }
        broadcastStatus()
    }

    private fun showNotification(): Notification {
        Log.d(LOGGER_TAG, "Showing notification")
        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.baseline_mic_24)
            setContentTitle(getString(R.string.app_name))
            setContentText(getString(R.string.recording_in_progress))
            setContentIntent(getOpenAppIntent())
            addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_stop_24,
                    getString(R.string.stop),
                    getStopRecordingIntent()
                )
            )
        }.build()
    }

    private fun getStopRecordingIntent(): PendingIntent {
        val stopRecordingIntent = Intent(context, AudioRecorderService::class.java)
        stopRecordingIntent.action = ACTION_STOP_RECORDING
        return PendingIntent.getService(
            context,
            STOP_RECORDING_REQUEST_CODE,
            stopRecordingIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getOpenAppIntent(): PendingIntent {
        val openAppIntent = Intent(context, MainActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
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
