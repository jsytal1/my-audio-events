package dev.syta.myaudioevents.ui.screens.recorder

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.syta.myaudioevents.AudioClassificationHelper
import dev.syta.myaudioevents.AudioClassificationListener
import dev.syta.myaudioevents.data.local.entities.EventType
import dev.syta.myaudioevents.ui.components.lazy.eventplot.PlotEventInfo
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.label.Category
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {
    var isRecording by mutableStateOf(false)
        private set
    var startTimeMs by mutableLongStateOf(System.currentTimeMillis() / 1000 * 1000)
        private set

    private val _events = mutableStateListOf<PlotEventInfo>()
    val events: List<PlotEventInfo>
        get() = _events

    private val _categoryToIndex = mutableMapOf<String, Int>()
    private val _categories = mutableStateListOf<EventType>()
    val categories: List<EventType>
        get() = _categories

    private val audioClassifierHelper by lazy {
        AudioClassificationHelper(context = applicationContext,
            overlap = .8f,
            numOfResults = 5,
            listener = object : AudioClassificationListener {
                override fun onError(error: String) {
                    // Handle error, e.g., show a Toast or update UI
                }

                override fun onResult(
                    results: List<Category>, inferenceTime: Long, timestampMs: Long
                ) {
                    results.forEach {
                        val label = it.label
                        if (!_categoryToIndex.containsKey(label)) {
                            _categoryToIndex[label] = _categories.size
                            _categories.add(EventType(it.index, label))
                        }
                        val event = PlotEventInfo(
                            timeMs = timestampMs,
                            labelIdx = _categoryToIndex[label]!!,
                            score = it.score
                        )
                        _events.add(event)
                    }
                }
            })
    }

    var showPermissionRationale by mutableStateOf(false)
        private set

    fun showPermissionRationale() {
        showPermissionRationale = true
    }

    fun hidePermissionRationale() {
        showPermissionRationale = false
    }

    fun startRecording() {
        viewModelScope.launch {
            startTimeMs = minOf(startTimeMs, System.currentTimeMillis())
            audioClassifierHelper.initClassifier()
            isRecording = true
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            audioClassifierHelper.stopAudioClassification()
            Toast.makeText(
                applicationContext, "Audio classification stopped", Toast.LENGTH_SHORT
            ).show()

            isRecording = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            audioClassifierHelper.stopAudioClassification()
        }
    }
}