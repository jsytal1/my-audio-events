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
    private val _showEditLabelDialog = MutableStateFlow(false)
    private val _showDeleteLabelDialog = MutableStateFlow(false)
    private val _selectedLabel = MutableStateFlow<Label?>(null)


    val uiState: StateFlow<LabelListScreenUiState> = combine(
        labelRepository.getLabels(),
        _showAddLabelDialog,
        _showEditLabelDialog,
        _showDeleteLabelDialog,
        _selectedLabel,
    ) { labels, showAddLabelDialog, showEditLabelDialog, showDeleteLabelDialog, selectedLabel ->
        LabelListScreenUiState.Ready(
            labels,
            showAddLabelDialog,
            showEditLabelDialog,
            showDeleteLabelDialog,
            selectedLabel,
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), LabelListScreenUiState.Loading
    )

    fun showAddLabelDialog() {
        _showAddLabelDialog.value = true
    }

    fun hideAddLabelDialog() {
        _showAddLabelDialog.value = false
    }

    fun showEditLabelDialog(label: Label) {
        _selectedLabel.value = label
        _showEditLabelDialog.value = true
    }

    fun hideEditLabelDialog() {
        _selectedLabel.value = null
        _showEditLabelDialog.value = false
    }

    fun addLabel(labelName: String) {
        if (labelName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.addLabel(Label(name = labelName))
        }
        hideAddLabelDialog()
    }

    fun editLabel(id: Int, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.editLabel(Label(id = id, name = name))
        }
        hideEditLabelDialog()
    }

    fun hideDeleteLabelDialog() {
        _selectedLabel.value = null
        _showDeleteLabelDialog.value = false
    }

    fun deleteLabel(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.deleteLabel(id)
        }
    }

    fun showDeleteLabelDialog(label: Label) {
        _selectedLabel.value = label
        _showDeleteLabelDialog.value = true
    }

    sealed interface LabelListScreenUiState {
        data object Loading : LabelListScreenUiState
        data class Ready(
            val list: List<Label>,
            val showAddLabelDialog: Boolean = false,
            val showEditLabelDialog: Boolean = false,
            val showDeleteLabelDialog: Boolean = false,
            val selectedLabel: Label? = null,
        ) : LabelListScreenUiState
    }

}
