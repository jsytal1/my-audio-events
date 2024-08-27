package dev.syta.myaudioevents.ui.screens.label_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.syta.myaudioevents.data.model.Label
import dev.syta.myaudioevents.data.repository.LabelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabelListViewModel @Inject constructor(
    private val labelRepository: LabelRepository
) : ViewModel() {
    private val _showAddLabelDialog = MutableStateFlow(false)

    val uiState: StateFlow<LabelListScreenUiState> = combine(
        labelRepository.getLabels(),
        _showAddLabelDialog
    ) { labels, showAddLabelDialog ->
        LabelListScreenUiState.Ready(labels, showAddLabelDialog)
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), LabelListScreenUiState.Loading
    )

    fun showAddLabelDialog() {
        _showAddLabelDialog.value = true
    }

    fun hideAddLabelDialog() {
        _showAddLabelDialog.value = false
    }

    fun addLabel(labelName: String) {
        if (labelName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.addLabel(Label(name = labelName))
        }
        hideAddLabelDialog()
    }

    sealed interface LabelListScreenUiState {
        data object Loading : LabelListScreenUiState
        data class Ready(
            val list: List<Label>,
            val showAddLabelDialog: Boolean = false
        ) : LabelListScreenUiState
    }

}
