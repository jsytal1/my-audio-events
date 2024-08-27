package dev.syta.myaudioevents.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.syta.myaudioevents.data.model.Label

@Entity(
    tableName = "label",
)
data class LabelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
)

fun LabelEntity.asExternalModel() = Label(
    id = id,
    name = name,
)

fun Label.asInternalModel() = LabelEntity(
    id = id,
    name = name,
)