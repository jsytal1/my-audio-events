package dev.syta.myaudioevents.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio_classes",
)
data class AudioClassEntity(
    @PrimaryKey val id: String,
)
