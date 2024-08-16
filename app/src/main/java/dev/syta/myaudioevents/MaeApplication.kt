package dev.syta.myaudioevents

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import dev.syta.myaudioevents.workers.SeedDatabaseWorker
import javax.inject.Inject

@HiltAndroidApp
class MaeApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()

        val workManager = WorkManager.getInstance(this)
        val seedDatabase = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()

        workManager.enqueueUniqueWork(
            SeedDatabaseWorker.WORK_NAME, ExistingWorkPolicy.KEEP, seedDatabase
        )
    }
}