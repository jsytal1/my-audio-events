package dev.syta.myaudioevents.data.repository

import android.content.res.AssetManager
import android.util.Log
import dev.syta.myaudioevents.data.local.dao.AudioRecordingDao
import dev.syta.myaudioevents.data.local.entities.AudioRecordingEntity
import dev.syta.myaudioevents.data.local.entities.asExternalModel
import dev.syta.myaudioevents.data.local.entities.asInternalModel
import dev.syta.myaudioevents.data.model.AudioRecording
import dev.syta.myaudioevents.utilities.AUDIO_RECORDING_PATH
import dev.syta.myaudioevents.utilities.formatMillisToReadableDate
import dev.syta.myaudioevents.utilities.getFileMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


interface AudioRecordingRepository {
    fun getAudioRecordings(): Flow<List<AudioRecording>>

    suspend fun insertAudioRecording(audioRecording: AudioRecording)
    suspend fun updateFromFile(audioRecording: AudioRecording)
    suspend fun createFromFiles(filesDir: File, assets: AssetManager)
    suspend fun createFromFile(file: File)
    suspend fun deleteAudioRecording(audioRecording: AudioRecording)
}

class AudioRecordingRepositoryImpl(
    private val audioRecordingDao: AudioRecordingDao,
) : AudioRecordingRepository {

    override fun getAudioRecordings(): Flow<List<AudioRecording>> =
        audioRecordingDao.getAllAudioRecordings()
            .map { it.map(AudioRecordingEntity::asExternalModel) }

    override suspend fun insertAudioRecording(audioRecording: AudioRecording) =
        audioRecordingDao.insertAudioRecording(audioRecording.asInternalModel())

    override suspend fun createFromFile(file: File) {
        val filePath = file.absolutePath
        val fileName = file.name.substringBeforeLast(".")
        val timestampMillis = fileName.toLong()
        val (fileSize, duration) = getFileMetadata(filePath)
        val formatedTimestamp = formatMillisToReadableDate(timestampMillis)
        val audioRecording = AudioRecording(
            filePath = filePath,
            name = "Audio Recording $formatedTimestamp",
            sizeBytes = fileSize.toInt(),
            durationMillis = duration.toInt(),
            timestampMillis = timestampMillis,
        )
        audioRecordingDao.insertAudioRecording(audioRecording.asInternalModel())
    }

    override suspend fun deleteAudioRecording(audioRecording: AudioRecording) {
        val file = File(audioRecording.filePath)
        if (file.exists()) {
            file.delete()
        }
        audioRecordingDao.deleteAudioRecording(audioRecording.id)
    }

    override suspend fun updateFromFile(audioRecording: AudioRecording) {
        val (fileSize, duration) = getFileMetadata(audioRecording.filePath)
        val updatedAudioFile = audioRecording.copy(
            sizeBytes = fileSize.toInt(), durationMillis = duration.toInt()
        )
        audioRecordingDao.updateAudioRecording(updatedAudioFile.asInternalModel())
    }

    override suspend fun createFromFiles(filesDir: File, assets: AssetManager) {
        Log.d("AudioRecordingRepository", "Creating audio recordings from assets")
        assets.list(AUDIO_RECORDING_PATH)?.forEach { assetFileName ->
            val assetFilePath = "$AUDIO_RECORDING_PATH/$assetFileName"
            val baseDir = File(filesDir, AUDIO_RECORDING_PATH)
            baseDir.mkdirs()
            val file = File(filesDir, assetFilePath)
            if (!file.exists()) {
                copyFileFromAssets(assets, assetFilePath, file.absolutePath)
                createFromFile(file)
            }
        }
    }
}

suspend fun copyFileFromAssets(
    assetManager: AssetManager,
    assetFilePath: String,
    outputFile: String
) {
    withContext(Dispatchers.IO) {
        val inputStream = assetManager.open(assetFilePath)
        val outputStream = FileOutputStream(outputFile)

        try {
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
        } catch (e: IOException) {
            e.printStackTrace() // Handle exceptions or throw them to be handled by a higher level
        } finally {
            try {
                inputStream.close()
                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
