package dev.syta.myaudioevents.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.syta.myaudioevents.data.local.entities.LabelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    @Query("SELECT * FROM label")
    fun getLabelEntities(): Flow<List<LabelEntity>>

    @Query("SELECT * FROM label WHERE id IN (:ids)")
    fun getLabelEntities(ids: List<String>): Flow<List<LabelEntity>>

    @Upsert
    fun upsertLabelEntity(label: LabelEntity)
}