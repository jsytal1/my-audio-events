package dev.syta.myaudioevents.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioRecord
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
import dev.syta.myaudioevents.data.repository.UserAudioClassRepository
import dev.syta.myaudioevents.utilities.AUDIO_RECORDING_PATH
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.audio.classifier.Classifications
import org.tensorflow.lite.task.core.BaseOptions
import java.io.File
import java.util.LinkedList
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

    @Inject
    lateinit var userAudioClassRepository: UserAudioClassRepository

    private lateinit var baseDir: String

    private var audioRecorderState: AudioRecorderState = AudioRecorderState.IDLE
    private var wavOutput: WavFile? = null
    private var modelFile: String = DEFAULT_MODEL
    private var scoreThreshold: Float = DEFAULT_SCORE_THRESHOLD
    private var maxResults: Int = DEFAULT_MAX_RESULTS
    private var overlap: Float = DEFAULT_OVERLAP_VALUE
    private var numThreads: Int = DEFAULT_NUM_THREADS
    private lateinit var recorder: AudioRecord
    private lateinit var classifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio
    private var classificationJob: Job? = null
    private var watchJob: Job? = null
    private var audioPacketBufferSize: Int = 0
    private var sampleRate: Int? = 0
    private var channelCount: Int? = 0
    private lateinit var audioBuffer: FloatArray
    private var audioBufferQueue: LinkedList<FloatArray> = LinkedList()
    private var maxBufferQueueSize = 2
    private var lastSaveTime = 0L
    private val saveExtraTime = 1000L

    private var followedAudioClassesNames = emptySet<String>()

    override fun onCreate() {
        super.onCreate()
        baseDir = "${context.filesDir}/$AUDIO_RECORDING_PATH"

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

        when (intent.action) {
            ACTION_STOP_RECORDING -> stopRecording()
            else -> startRecording()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        watchJob?.cancel()
        stopRecording()
        stopSelf()
        isRunning = false
    }

    private fun initClassifier() {
        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)
        val options =
            AudioClassifier.AudioClassifierOptions.builder().setScoreThreshold(scoreThreshold)
                .setMaxResults(maxResults).setBaseOptions(baseOptionsBuilder.build()).build()
        try {
            classifier = AudioClassifier.createFromFileAndOptions(
                applicationContext, modelFile, options
            )
            tensorAudio = classifier.createInputTensorAudio()
            recorder = classifier.createAudioRecord()
            sampleRate = recorder.sampleRate
            channelCount = recorder.channelCount
        } catch (e: Exception) {
            Log.e(LOGGER_TAG, "Classifier failed to load with error: " + e.message)
        }
    }

    private fun startRecording() {
        val recordingsDir = File(baseDir)
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }

        isRunning = true
        if (audioRecorderState == AudioRecorderState.RECORDING) {
            return
        }
        initClassifier()
        initializeAudioBuffer()
        recorder.startRecording()

        watchJob = CoroutineScope(Dispatchers.IO).launch {
            userAudioClassRepository.followedAudioClassNames().collect { latestNames ->
                followedAudioClassesNames = latestNames
            }
        }

        classificationJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                loadAudio()
                val results = classifier.classify(tensorAudio)[0]

                maybeSaveAudio(results)
            }
        }
        audioRecorderState = AudioRecorderState.RECORDING
        startForeground(FOREGROUND_NOTIFICATION_ID, showNotification())

        broadcastStatus()
    }

    private fun maybeSaveAudio(results: Classifications) {
        val shouldSave = shouldSaveForResults(results)
        val currTime = System.currentTimeMillis()
        if (shouldSave) {
            if (wavOutput == null) {
                wavOutput = WavFile(
                    baseDir = baseDir,
                    fileName = createFilename(),
                    channelCount = channelCount!!.toShort(),
                    sampleRate = sampleRate!!
                )
                writeBufferQueue()
            }

            lastSaveTime = currTime
            writeBuffer(audioBuffer)
        } else if (saveExtraTime > currTime - lastSaveTime) {
            writeBuffer(audioBuffer)
        } else {
            finalizeRecording()
            if (audioBufferQueue.size == maxBufferQueueSize) {
                audioBufferQueue.poll()
            }
            audioBufferQueue.add(audioBuffer.copyOf())
        }
    }


    private fun writeBufferQueue() {
        while (audioBufferQueue.isNotEmpty()) {
            val buffer = audioBufferQueue.poll()
            buffer?.let { writeBuffer(it) }
        }
    }

    private fun writeBuffer(buffer: FloatArray) {
        buffer.let {
            wavOutput!!.write(it, buffer.size)
        }
    }


    private fun shouldSaveForResults(results: Classifications?): Boolean {
        val categories = results?.categories ?: emptyList()
        if (followedAudioClassesNames.isEmpty()) {
            return false
        }
        return categories.any { it.label in followedAudioClassesNames }
    }


    private fun initializeAudioBuffer() {
        audioPacketBufferSize = (classifier.requiredInputBufferSize * (1 - overlap)).toInt()
        audioBuffer = FloatArray(audioPacketBufferSize)
    }

    private fun loadAudio(): Int {
        val readSize =
            recorder.read(audioBuffer, 0, audioPacketBufferSize, AudioRecord.READ_BLOCKING)
        if (readSize > 0) {
            tensorAudio.load(audioBuffer, 0, readSize)
        }
        return readSize
    }


    private fun showNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.baseline_mic_24)
            setContentTitle(getString(R.string.app_name))
            setContentText(getString(R.string.recording_in_progress))
            setContentIntent(getOpenAppIntent())
            addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_stop_24, getString(R.string.stop), getStopRecordingIntent()
                )
            )
        }.build()
    }

    private fun getStopRecordingIntent(): PendingIntent {
        val stopRecordingIntent = Intent(context, AudioRecorderService::class.java)
        stopRecordingIntent.action = ACTION_STOP_RECORDING
        return PendingIntent.getService(
            context, STOP_RECORDING_REQUEST_CODE, stopRecordingIntent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getOpenAppIntent(): PendingIntent {
        val openAppIntent = Intent(context, MainActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            context, OPEN_APP_REQUEST_CODE, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopRecording() {
        if (audioRecorderState == AudioRecorderState.RECORDING) {
            recorder.stop()
            recorder.release()
            classificationJob?.cancel()

            finalizeRecording()
            audioRecorderState = AudioRecorderState.IDLE
        }
        broadcastStatus()
    }

    private fun finalizeRecording() {
        if (wavOutput == null) {
            return
        }

        wavOutput!!.close()
        val durationMillis = wavOutput!!.durationMillis
        val startTime = System.currentTimeMillis() - durationMillis
        val name = "Recording at $startTime"
        val filePath = wavOutput!!.file.absolutePath
        val sizeBytes = wavOutput!!.sizeBytes
        CoroutineScope(Dispatchers.IO).launch {
            saveRecordingToDatabase(
                name = name,
                filePath = filePath,
                durationMillis = durationMillis,
                sizeBytes = sizeBytes
            )
        }
        wavOutput = null
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
        EventBus.getDefault().post(Events.AudioRecorderState(audioRecorderState))
    }

    private fun createFilename(): String {
        return "${System.currentTimeMillis()}.wav"
    }

    companion object {
        var isRunning = false

        const val STOP_RECORDING_REQUEST_CODE = 1
        const val OPEN_APP_REQUEST_CODE = 2

        const val DEFAULT_MODEL = "yamnet.tflite"
        const val DEFAULT_SCORE_THRESHOLD = 0.3f
        const val DEFAULT_MAX_RESULTS = 10
        const val DEFAULT_OVERLAP_VALUE = 0.5f
        const val DEFAULT_NUM_THREADS = 2
    }
}