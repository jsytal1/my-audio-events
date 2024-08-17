package dev.syta.myaudioevents.data.repository

import dev.syta.myaudioevents.data.datastore.MaePreferencesDataSource
import dev.syta.myaudioevents.data.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserDataRepository {
    val userData: Flow<UserData>

    suspend fun setAudioClassIdFollowed(followedTopicId: String, followed: Boolean)
}

internal class OfflineFirstUserDataRepository @Inject constructor(
    private val maePreferencesDataSource: MaePreferencesDataSource,
) : UserDataRepository {

    override val userData: Flow<UserData> =
        maePreferencesDataSource.userData

    override suspend fun setAudioClassIdFollowed(followedTopicId: String, followed: Boolean) {
        maePreferencesDataSource.setAudioClassIdFollowed(followedTopicId, followed)

    }
}
