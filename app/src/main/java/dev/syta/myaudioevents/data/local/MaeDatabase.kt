package dev.syta.myaudioevents.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.syta.myaudioevents.data.local.dao.AudioClassDao
import dev.syta.myaudioevents.data.local.dao.AudioRecordingDao
import dev.syta.myaudioevents.data.local.entities.AudioClassAncestorCrossRef
import dev.syta.myaudioevents.data.local.entities.AudioClassEntity
import dev.syta.myaudioevents.data.local.entities.AudioRecordingEntity

@Database(
    entities = [
        AudioClassEntity::class,
        AudioClassAncestorCrossRef::class,
        AudioRecordingEntity::class,
    ],
    version = 1,
)
abstract class MaeDatabase : RoomDatabase() {
    abstract fun audioClassDao(): AudioClassDao

    abstract fun audioRecordingDao(): AudioRecordingDao
}