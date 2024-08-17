package dev.syta.myaudioevents.data.repository

import dev.syta.myaudioevents.data.model.UserAudioClass
import dev.syta.myaudioevents.data.model.mapToUserAudioClasses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

interface UserAudioClassRepository {
    fun observeAll(): Flow<List<UserAudioClass>>
}

/**
 * Implements a [UserAudioClassRepository] by combining a [AudioClassRepository] with a
 * [UserDataRepository].
 */
class CompositeUserAudioClassRepository @Inject constructor(
    val audioClassRepository: AudioClassRepository,
    val userDataRepository: UserDataRepository,
) : UserAudioClassRepository {

    /**
     * Returns available audio classes (joined with user data).
     */
    override fun observeAll(): Flow<List<UserAudioClass>> = audioClassRepository.getAudioClasses()
        .combine(userDataRepository.userData) { audioClasses, userData ->
            audioClasses.mapToUserAudioClasses(userData)
        }
}