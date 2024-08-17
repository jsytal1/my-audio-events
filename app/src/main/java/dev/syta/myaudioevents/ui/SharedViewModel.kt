package dev.syta.myaudioevents.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MaeSharedViewModel @Inject constructor() : ViewModel() {
    private val _fabState = MutableLiveData<FabState>()
    val fabState: LiveData<FabState> = _fabState

    fun showFab(
        icon: ImageVector,
        contentDescriptionId: Int,
        onClick: () -> Unit,
    ) {
        _fabState.value = FabState(true, icon, contentDescriptionId, onClick)
    }

    fun hideFab() {
        _fabState.value = FabState(false, null, null) {}
    }

    data class FabState(
        val isVisible: Boolean,
        val icon: ImageVector?,
        val contentDescriptionId: Int?,
        val onClick: () -> Unit,
    )
}
