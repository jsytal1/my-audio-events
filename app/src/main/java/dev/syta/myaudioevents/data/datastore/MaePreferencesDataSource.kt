package dev.syta.myaudioevents.data.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import dev.syta.myaudioevents.UserPreferences
import dev.syta.myaudioevents.copy
import dev.syta.myaudioevents.data.model.UserData
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class MaePreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>
) {
    val userData = userPreferences.data
        .map {
            UserData(
                followedAudioClasses = it.followedAudioClassIdsMap.keys
            )
        }

    suspend fun setAudioClassIdFollowed(audioClassId: String, followed: Boolean) {
        try {
            userPreferences.updateData {
                it.copy {
                    if (followed) {
                        followedAudioClassIds.put(audioClassId, true)
                    } else {
                        followedAudioClassIds.remove(audioClassId)
                    }
                }
            }
        } catch (ioException: IOException) {
            Log.e("MaePreferences", "Failed to update user preferences", ioException)
        }
    }
}