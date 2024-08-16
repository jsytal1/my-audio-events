package dev.syta.myaudioevents.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.syta.myaudioevents.data.local.MaeDatabase
import dev.syta.myaudioevents.data.local.entities.AudioClassAncestorCrossRef
import dev.syta.myaudioevents.data.local.entities.AudioClassEntity
import dev.syta.myaudioevents.utilities.ONTOLOGY_ANCESTOR_DATA_FILE
import dev.syta.myaudioevents.utilities.ONTOLOGY_DATA_FILE
import kotlinx.coroutines.coroutineScope

@HiltWorker
class SeedDatabaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: MaeDatabase
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            applicationContext.assets.open(ONTOLOGY_DATA_FILE).use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val entityListType = object : TypeToken<List<AudioClassEntity>>() {}.type
                    val entityList: List<AudioClassEntity> =
                        Gson().fromJson(jsonReader, entityListType)

                    database.audioClassDao().upsertAudioClassEntities(entityList)
                }
            }
            applicationContext.assets.open(ONTOLOGY_ANCESTOR_DATA_FILE).use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val entityListType =
                        object : TypeToken<List<AudioClassAncestorCrossRef>>() {}.type
                    val entityList: List<AudioClassAncestorCrossRef> =
                        Gson().fromJson(jsonReader, entityListType)

                    database.audioClassDao().upsertAudioClassAncestorCrossRefs(entityList)
                }
            }
            Result.success()

        } catch (ex: Exception) {
            Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME: String = "seed_database"
        private const val TAG = "SeedDatabaseWorker"
    }
}