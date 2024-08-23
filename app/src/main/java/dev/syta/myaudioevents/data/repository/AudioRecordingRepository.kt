package dev.syta.myaudioevents.data.repository

import dev.syta.myaudioevents.data.local.dao.AudioRecordingDao
import dev.syta.myaudioevents.data.local.entities.AudioRecordingEntity
import dev.syta.myaudioevents.data.local.entities.asExternalModel
import dev.syta.myaudioevents.data.local.entities.asInternalModel
import dev.syta.myaudioevents.data.model.AudioRecording
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AudioRecordingRepository {
    fun getAudioRecordings(): Flow<List<AudioRecording>>

    suspend fun insertAudioRecording(audioRecording: AudioRecording)
}

class AudioRecordingRepositoryImpl(
    private val audioRecordingDao: AudioRecordingDao,
) : AudioRecordingRepository {

    override fun getAudioRecordings(): Flow<List<AudioRecording>> =
        audioRecordingDao.getAllAudioRecordings()
            .map { it.map(AudioRecordingEntity::asExternalModel) }

    override suspend fun insertAudioRecording(audioRecording: AudioRecording) =
        audioRecordingDao.insertAudioRecording(audioRecording.asInternalModel())

}