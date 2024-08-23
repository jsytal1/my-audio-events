package dev.syta.myaudioevents.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.syta.myaudioevents.data.model.AudioRecording

@Entity(
    tableName = "audio_recordings",
)
data class AudioRecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "timestamp_millis") val timestampMillis: Long,
    @ColumnInfo(name = "duration_millis") val durationMillis: Int,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Int,
)

fun AudioRecordingEntity.asExternalModel() = AudioRecording(
    id = id,
    name = name,
    filePath = filePath,
    timestampMillis = timestampMillis,
    durationMillis = durationMillis,
    sizeBytes = sizeBytes,
)

fun AudioRecording.asInternalModel() = AudioRecordingEntity(
    id = id,
    name = name,
    filePath = filePath,
    timestampMillis = timestampMillis,
    durationMillis = durationMillis,
    sizeBytes = sizeBytes,
)