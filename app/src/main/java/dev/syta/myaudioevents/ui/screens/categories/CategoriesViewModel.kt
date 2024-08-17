package dev.syta.myaudioevents.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.syta.myaudioevents.data.model.UserAudioClass
import dev.syta.myaudioevents.data.repository.UserAudioClassRepository
import dev.syta.myaudioevents.data.repository.UserDataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    userAudioClassRepository: UserAudioClassRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    private val audioClassesFlow = userAudioClassRepository.observeAll()

    val uiState = combine(
        audioClassesFlow,
    ) { (audioClasses) ->
        if (audioClasses.isEmpty()) {
            CategoriesScreenUiState.Loading
        } else {
            CategoriesScreenUiState.Ready(
                audioClassList = audioClasses,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CategoriesScreenUiState.Loading,
    )

    fun followAudioClass(audioClassId: String, followed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setAudioClassIdFollowed(audioClassId, followed)
        }
    }
}

sealed interface CategoriesScreenUiState {
    data object Loading : CategoriesScreenUiState
    data class Ready(
        val audioClassList: List<UserAudioClass>,
    ) : CategoriesScreenUiState
}