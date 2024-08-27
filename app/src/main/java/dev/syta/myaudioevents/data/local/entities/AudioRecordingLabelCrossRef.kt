package dev.syta.myaudioevents.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "audio_recording_labels",
    primaryKeys = ["recording_id", "label_id"],
    indices = [Index(value = ["recording_id"])]
)
data class AudioRecordingLabelCrossRef(
    @ColumnInfo(name = "recording_id") val recordingId: Int,
    @ColumnInfo(name = "label_id") val labelId: Int,
)