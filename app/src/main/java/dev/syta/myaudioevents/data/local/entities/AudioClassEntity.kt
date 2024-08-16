package dev.syta.myaudioevents.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.syta.myaudioevents.data.model.AudioClass

@Entity(
    tableName = "audio_classes",
)
data class AudioClassEntity(
    @PrimaryKey val id: String,
    val name: String,
)

fun AudioClassEntity.asExternalModel() = AudioClass(
    id = id,
    name = name,
    ancestors = emptyList(),
)