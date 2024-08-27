package dev.syta.myaudioevents.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.syta.myaudioevents.data.local.dao.AudioClassDao
import dev.syta.myaudioevents.data.local.dao.AudioRecordingDao
import dev.syta.myaudioevents.data.local.dao.LabelDao
import dev.syta.myaudioevents.data.local.entities.AudioClassAncestorCrossRef
import dev.syta.myaudioevents.data.local.entities.AudioClassEntity
import dev.syta.myaudioevents.data.local.entities.AudioRecordingEntity
import dev.syta.myaudioevents.data.local.entities.LabelEntity

@Database(
    entities = [
        AudioClassEntity::class,
        AudioClassAncestorCrossRef::class,
        AudioRecordingEntity::class,
        LabelEntity::class,
    ], version = 1, exportSchema = true
)
abstract class MaeDatabase : RoomDatabase() {
    abstract fun audioClassDao(): AudioClassDao

    abstract fun audioRecordingDao(): AudioRecordingDao

    abstract fun labelDao(): LabelDao
}