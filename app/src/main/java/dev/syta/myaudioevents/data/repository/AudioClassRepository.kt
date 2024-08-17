package dev.syta.myaudioevents.data.repository

import dev.syta.myaudioevents.data.local.dao.AudioClassDao
import dev.syta.myaudioevents.data.local.entities.PopulatedAudioClass
import dev.syta.myaudioevents.data.local.entities.asExternalModel
import dev.syta.myaudioevents.data.model.AudioClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AudioClassRepository {
    fun getAudioClasses(): Flow<List<AudioClass>>
}

class OfflineFirstAudioClassRepository constructor(
    private val audioClassDao: AudioClassDao
) : AudioClassRepository {
    override fun getAudioClasses(): Flow<List<AudioClass>> =
        audioClassDao.getPopulatedAudioClasses()
            .map { it.map(PopulatedAudioClass::asExternalModel) }
}