package dev.syta.myaudioevents.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import dev.syta.myaudioevents.data.local.entities.AudioClassAncestorCrossRef
import dev.syta.myaudioevents.data.local.entities.AudioClassEntity
import dev.syta.myaudioevents.data.local.entities.PopulatedAudioClass
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioClassDao {
    @Query("SELECT * FROM audio_classes")
    fun getAudioClassEntities(): Flow<List<AudioClassEntity>>

    @Transaction
    @Query("SELECT * FROM audio_classes")
    fun getPopulatedAudioClasses(): Flow<List<PopulatedAudioClass>>

    @Query("SELECT * FROM audio_classes WHERE id IN (:ids)")
    fun getPopulatedAudioClasses(ids: List<String>): Flow<List<PopulatedAudioClass>>

    @Upsert
    fun upsertAudioClassEntities(audioClasses: List<AudioClassEntity>)

    @Upsert
    fun upsertAudioClassAncestorCrossRefs(audioClassAncestorCrossRefs: List<AudioClassAncestorCrossRef>)

}