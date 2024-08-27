package dev.syta.myaudioevents.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.syta.myaudioevents.data.local.entities.AudioRecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordingDao {

    @Query("SELECT * FROM audio_recordings")
    fun getAllAudioRecordings(): Flow<List<AudioRecordingEntity>>

    @Insert
    suspend fun insertAudioRecording(recording: AudioRecordingEntity)

    @Update
    suspend fun updateAudioRecording(recording: AudioRecordingEntity)

    @Query("DELETE FROM audio_recordings WHERE id = :id")
    suspend fun deleteAudioRecording(id: Int)
}