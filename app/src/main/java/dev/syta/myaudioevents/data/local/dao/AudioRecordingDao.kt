package dev.syta.myaudioevents.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import dev.syta.myaudioevents.data.local.entities.AudioRecordingEntity
import dev.syta.myaudioevents.data.local.entities.AudioRecordingLabelCrossRef
import dev.syta.myaudioevents.data.local.entities.PopulatedAudioRecording
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordingDao {

    @Query("SELECT * FROM audio_recordings")
    fun getAllAudioRecordings(): Flow<List<AudioRecordingEntity>>

    @Transaction
    @Query("SELECT * FROM audio_recordings")
    fun getPopulatedAudioRecordings(): Flow<List<PopulatedAudioRecording>>

    @Insert
    suspend fun insertAudioRecording(recording: AudioRecordingEntity)

    @Update
    suspend fun updateAudioRecording(recording: AudioRecordingEntity)

    @Query("DELETE FROM audio_recordings WHERE id = :id")
    suspend fun deleteAudioRecording(id: Int)

    @Upsert
    fun upsertAudioRecordingLabelCrossRefs(audioRecordingLabelCrossRefs: List<AudioRecordingLabelCrossRef>)

    @Query("DELETE FROM audio_recording_labels WHERE recording_id = :recordingId AND label_id IN (:labelIds)")
    fun deleteAudioRecordingLabelCrossRefs(recordingId: Int, labelIds: List<Int>)
}