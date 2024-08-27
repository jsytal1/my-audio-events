package dev.syta.myaudioevents.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dev.syta.myaudioevents.data.local.dao.AudioClassDao
import dev.syta.myaudioevents.data.local.dao.AudioRecordingDao
import dev.syta.myaudioevents.data.local.dao.LabelDao
import dev.syta.myaudioevents.data.local.entities.AudioClassAncestorCrossRef
import dev.syta.myaudioevents.data.local.entities.AudioClassEntity
import dev.syta.myaudioevents.data.local.entities.AudioRecordingEntity
import dev.syta.myaudioevents.data.local.entities.AudioRecordingLabelCrossRef
import dev.syta.myaudioevents.data.local.entities.LabelEntity
import dev.syta.myaudioevents.workers.SeedDatabaseWorker

@Database(
    entities = [
        AudioClassEntity::class,
        AudioClassAncestorCrossRef::class,
        AudioRecordingEntity::class,
        LabelEntity::class,
        AudioRecordingLabelCrossRef::class,
    ], autoMigrations = [], version = 1, exportSchema = true
)
abstract class MaeDatabase : RoomDatabase() {
    abstract fun audioClassDao(): AudioClassDao
    abstract fun audioRecordingDao(): AudioRecordingDao
    abstract fun labelDao(): LabelDao

    companion object {
        private var instance: MaeDatabase? = null

        @Synchronized
        fun getInstance(context: Context): MaeDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext, MaeDatabase::class.java, "my_database"
                ).addCallback(callback(context)).build()
            }
            return instance!!
        }

        private fun callback(context: Context): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    enqueueSeedDatabaseWorker(context)
                }
            }
        }

        private fun enqueueSeedDatabaseWorker(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val seedDatabase = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()

            workManager.enqueueUniqueWork(
                SeedDatabaseWorker.WORK_NAME, ExistingWorkPolicy.KEEP, seedDatabase
            )
        }
    }
}