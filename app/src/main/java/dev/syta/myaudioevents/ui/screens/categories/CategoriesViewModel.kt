package dev.syta.myaudioevents.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.syta.myaudioevents.data.model.AudioClass
import dev.syta.myaudioevents.data.repository.AudioClassStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    audioClassStore: AudioClassStore,
) : ViewModel() {
    private val audioClassesFlow = audioClassStore.getAudioClasses()

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
}

sealed interface CategoriesScreenUiState {
    data object Loading : CategoriesScreenUiState
    data class Ready(
        val audioClassList: List<AudioClass>,
    ) : CategoriesScreenUiState
}