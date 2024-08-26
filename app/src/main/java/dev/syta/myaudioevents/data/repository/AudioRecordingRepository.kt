package dev.syta.myaudioevents.data.repository

import dev.syta.myaudioevents.data.local.dao.AudioRecordingDao
import dev.syta.myaudioevents.data.local.entities.AudioRecordingEntity
import dev.syta.myaudioevents.data.local.entities.asExternalModel
import dev.syta.myaudioevents.data.local.entities.asInternalModel
import dev.syta.myaudioevents.data.model.AudioRecording
import dev.syta.myaudioevents.utilities.getFileMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AudioRecordingRepository {
    fun getAudioRecordings(): Flow<List<AudioRecording>>

    suspend fun insertAudioRecording(audioRecording: AudioRecording)
    suspend fun updateFromFile(audioRecording: AudioRecording)
}

class AudioRecordingRepositoryImpl(
    private val audioRecordingDao: AudioRecordingDao,
) : AudioRecordingRepository {

    override fun getAudioRecordings(): Flow<List<AudioRecording>> =
        audioRecordingDao.getAllAudioRecordings()
            .map { it.map(AudioRecordingEntity::asExternalModel) }

    override suspend fun insertAudioRecording(audioRecording: AudioRecording) =
        audioRecordingDao.insertAudioRecording(audioRecording.asInternalModel())

    override suspend fun updateFromFile(audioRecording: AudioRecording) {

        val (fileSize, duration) = getFileMetadata(audioRecording.filePath)
        val updatedAudioFile = audioRecording.copy(
            sizeBytes = fileSize.toInt(),
            durationMillis = duration.toInt()
        )
        audioRecordingDao.updateAudioRecording(updatedAudioFile.asInternalModel())
    }
}