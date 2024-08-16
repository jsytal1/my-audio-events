package dev.syta.myaudioevents.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "audio_class_ancestors",
    primaryKeys = ["descendant_id", "ancestor_id"],
    indices = [Index(value = ["ancestor_id"])]
)
data class AudioClassAncestorCrossRef(
    @ColumnInfo(name = "descendant_id") val descendantId: String,
    @ColumnInfo(name = "ancestor_id") val ancestorId: String,
)