package dev.syta.myaudioevents.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import dev.syta.myaudioevents.data.model.AudioRecording

data class PopulatedAudioRecording(
    @Embedded val entity: AudioRecordingEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = AudioRecordingLabelCrossRef::class,
            parentColumn = "recording_id",
            entityColumn = "label_id",
        )
    ) val labels: List<LabelEntity>,
)

fun PopulatedAudioRecording.asExternalModel() = AudioRecording(
    id = entity.id,
    name = entity.name,
    filePath = entity.filePath,
    timestampMillis = entity.timestampMillis,
    durationMillis = entity.durationMillis,
    sizeBytes = entity.sizeBytes,
    labels = labels.map { it.asExternalModel() },
)