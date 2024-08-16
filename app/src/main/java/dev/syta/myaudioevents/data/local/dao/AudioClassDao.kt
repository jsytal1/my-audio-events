package dev.syta.myaudioevents.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.syta.myaudioevents.data.local.entities.AudioClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioClassDao {
    @Query("SELECT * FROM audio_classes")
    fun getAudioClassEntities(): Flow<List<AudioClassEntity>>

    @Upsert
    fun upsertAudioClassEntities(audioClasses: List<AudioClassEntity>)
}