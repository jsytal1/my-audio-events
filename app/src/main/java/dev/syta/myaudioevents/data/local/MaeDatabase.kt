package dev.syta.myaudioevents.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.syta.myaudioevents.data.local.dao.AudioClassDao
import dev.syta.myaudioevents.data.local.entities.AudioClassEntity

@Database(
    entities = [
        AudioClassEntity::class,
    ],
    version = 1,
)
abstract class MaeDatabase : RoomDatabase() {
    abstract fun audioClassDao(): AudioClassDao
}