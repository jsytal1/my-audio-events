package dev.syta.myaudioevents.ui.screens.categories

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.syta.myaudioevents.data.repository.AudioClassStore
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val audioClassStore: AudioClassStore,
) : ViewModel() {
}

