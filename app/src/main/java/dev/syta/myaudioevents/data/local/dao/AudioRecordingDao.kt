package dev.syta.myaudioevents.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.syta.myaudioevents.data.local.entities.AudioRecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordingDao {

    @Query("SELECT * FROM audio_recordings")
    fun getAllAudioRecordings(): Flow<List<AudioRecordingEntity>>

    @Insert
    suspend fun insertAudioRecording(recording: AudioRecordingEntity)

}