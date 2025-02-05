/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.syta.myaudioevents

import android.content.Context
import android.media.AudioRecord
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.core.BaseOptions
import java.util.concurrent.ScheduledThreadPoolExecutor

interface AudioClassificationListener {
    fun onError(error: String)
    fun onResult(
        results: List<Category>, inferenceTime: Long, timestampMs: Long, audioBuffer: FloatArray
    )
}

class AudioClassificationHelper(
    val context: Context,
    val listener: AudioClassificationListener,
    private var currentModel: String = YAMNET_MODEL,
    private var classificationThreshold: Float = DISPLAY_THRESHOLD,
    var overlap: Float = DEFAULT_OVERLAP_VALUE,
    var numOfResults: Int = DEFAULT_NUM_OF_RESULTS,
    private var currentDelegate: Int = 0,
    private var numThreads: Int = 2
) {
    private lateinit var classifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio
    private lateinit var recorder: AudioRecord
    private var readStartTimeMs: Long = Long.MIN_VALUE
    private var totalReadSize: Long = 0
    private lateinit var executor: ScheduledThreadPoolExecutor
    private var classificationJob: Job? = null
    private var bufferSize = 0

    fun recorderChannelCount(): Int {
        return recorder.channelCount
    }

    fun recorderSampleRate(): Int {
        return recorder.sampleRate
    }

    fun initClassifier() {
        // Set general detection options, e.g. number of used threads
        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)

        // Use the specified hardware for running the model. Default to CPU.
        // Possible to also use a GPU delegate, but this requires that the classifier be created
        // on the same thread that is using the classifier, which is outside of the scope of this
        // sample's design.
        when (currentDelegate) {
            DELEGATE_CPU -> {
                // Default
            }

            DELEGATE_NNAPI -> {
                baseOptionsBuilder.useNnapi()
            }
        }

        // Configures a set of parameters for the classifier and what results will be returned.
        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setScoreThreshold(classificationThreshold).setMaxResults(numOfResults)
            .setBaseOptions(baseOptionsBuilder.build()).build()

        try {
            // Create the classifier and required supporting objects
            classifier = AudioClassifier.createFromFileAndOptions(context, currentModel, options)
            tensorAudio = classifier.createInputTensorAudio()
            recorder = classifier.createAudioRecord()
            startAudioClassification()
        } catch (e: IllegalStateException) {
            listener.onError(
                "Audio Classifier failed to initialize. See error logs for details"
            )

            Log.e("AudioClassification", "TFLite failed to load with error: " + e.message)
        }
    }

    private fun startAudioClassification() {
        if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            return
        }

        recorder.startRecording()
        readStartTimeMs = System.currentTimeMillis()
        totalReadSize = 0
        executor = ScheduledThreadPoolExecutor(1)

        bufferSize = (classifier.requiredInputBufferSize * (1 - overlap)).toInt()

        classificationJob = CoroutineScope(Dispatchers.Default).launch {
            // Adjust based on the bit depth and channels.

            while (isActive) {
                classifyAudio()
            }
        }
    }

    private fun classifyAudio() {
        val audioBuffer = FloatArray(bufferSize)
        val result = recorder.read(audioBuffer, 0, bufferSize, AudioRecord.READ_BLOCKING)
        if (result < 0) {
            Log.e("AudioRecord", "Error reading audio")
            return
        }

        if (result != bufferSize) {
            // skip for now for simplicity
            return
        }

        totalReadSize += result
        tensorAudio.load(audioBuffer, 0, result)

        val bufferEndTimeMs =
            readStartTimeMs + (totalReadSize * 1000 / tensorAudio.format.sampleRate)
        var inferenceTime = SystemClock.uptimeMillis()
        val output = classifier.classify(tensorAudio)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        listener.onResult(output[0].categories, inferenceTime, bufferEndTimeMs, audioBuffer)
    }

    fun stopAudioClassification() {
        recorder.stop()
        classificationJob?.cancel()
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_NNAPI = 1
        const val DISPLAY_THRESHOLD = 0.3f
        const val DEFAULT_NUM_OF_RESULTS = 2
        const val DEFAULT_OVERLAP_VALUE = 0.5f
        const val YAMNET_MODEL = "yamnet.tflite"
    }
}