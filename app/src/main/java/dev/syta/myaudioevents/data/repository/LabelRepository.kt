package dev.syta.myaudioevents.data.repository

import dev.syta.myaudioevents.data.local.dao.LabelDao
import dev.syta.myaudioevents.data.local.entities.LabelEntity
import dev.syta.myaudioevents.data.local.entities.asExternalModel
import dev.syta.myaudioevents.data.local.entities.asInternalModel
import dev.syta.myaudioevents.data.model.Label
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LabelRepository {
    fun getLabels(): Flow<List<Label>>

    fun getLabels(ids: Set<String>): Flow<List<Label>>

    fun addLabel(label: Label)

    fun editLabel(label: Label)

    fun deleteLabel(id: Int)
}

class OfflineFirstLabelRepository(private val labelDao: LabelDao) : LabelRepository {
    override fun getLabels(): Flow<List<Label>> =
        labelDao.getLabelEntities().map { it.map(LabelEntity::asExternalModel) }

    override fun getLabels(ids: Set<String>): Flow<List<Label>> =
        labelDao.getLabelEntities(ids.toList()).map { it.map(LabelEntity::asExternalModel) }

    override fun addLabel(label: Label) {
        labelDao.upsertLabelEntity(label.asInternalModel())
    }

    override fun editLabel(label: Label) {
        labelDao.upsertLabelEntity(label.asInternalModel())
    }

    override fun deleteLabel(id: Int) {
        labelDao.deleteLabelEntity(id)
    }
}