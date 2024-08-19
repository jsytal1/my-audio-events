package dev.syta.myaudioevents.data.repository

import dev.syta.myaudioevents.data.model.UserAudioClass
import dev.syta.myaudioevents.data.model.mapToUserAudioClasses
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserAudioClassRepository {
    fun observeAll(): Flow<List<UserAudioClass>>

    fun followedAudioClassNames(): Flow<Set<String>>
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun followedAudioClassNames(): Flow<Set<String>> =
        userDataRepository.userData.flatMapConcat { userData ->
            audioClassRepository.getAudioClasses(ids = userData.followedAudioClasses)
                .map { audioClasses ->
                    audioClasses.map { it.name }.toSet()
                }
        }

}