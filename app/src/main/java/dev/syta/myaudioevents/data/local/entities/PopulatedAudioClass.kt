package dev.syta.myaudioevents.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import dev.syta.myaudioevents.data.model.AudioClass

data class PopulatedAudioClass(
    @Embedded val entity: AudioClassEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = AudioClassAncestorCrossRef::class,
            parentColumn = "descendant_id",
            entityColumn = "ancestor_id",
        ),
        projection = ["id", "name"],
    ) val ancestors: List<AudioClassEntity>,
)

fun PopulatedAudioClass.asExternalModel() = AudioClass(
    id = entity.id,
    name = entity.name,
    ancestors = ancestors.map { it.name },
)