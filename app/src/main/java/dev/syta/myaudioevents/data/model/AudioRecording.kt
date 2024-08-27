package dev.syta.myaudioevents.data.model

data class AudioRecording(
    val id: Int = 0,
    val name: String,
    val filePath: String,
    val timestampMillis: Long,
    val durationMillis: Int,
    val sizeBytes: Int,
    val labels: List<Label>,
)

