package dev.syta.myaudioevents.ui.screens.categories

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import dev.syta.myaudioevents.data.model.AudioClass

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val s = uiState) {
        CategoriesScreenUiState.Loading -> {
            Text("Loading")
        }

        is CategoriesScreenUiState.Ready -> {
            CategoriesList(
                audioClassList = s.audioClassList,
            )
        }
    }
}

@Composable
fun CategoriesList(audioClassList: List<AudioClass>) {
    LazyColumn {
        items(audioClassList) { audioClass ->
            Text(audioClass.name)
        }
    }
}

