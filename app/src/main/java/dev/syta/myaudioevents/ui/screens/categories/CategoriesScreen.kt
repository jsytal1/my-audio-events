package dev.syta.myaudioevents.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.syta.myaudioevents.data.model.UserAudioClass
import dev.syta.myaudioevents.ui.components.MaeTag


@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {
            CategoriesScreenContent(
                uiState = uiState,
                followAudioClass = viewModel::followAudioClass,
            )
        }
    }
}

@Composable
fun CategoriesScreenContent(
    uiState: CategoriesScreenUiState,
    followAudioClass: (String, Boolean) -> Unit,
) {
    when (val s = uiState) {
        CategoriesScreenUiState.Loading -> {
            Text("Loading")
        }

        is CategoriesScreenUiState.Ready -> {

            CategoriesList(
                audioClassList = s.audioClassList,
                followAudioClass = followAudioClass,
            )
        }
    }
}


@Composable
fun CategoriesList(
    audioClassList: List<UserAudioClass>,
    followAudioClass: (String, Boolean) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(audioClassList) {
            CategoryCard(
                it,
                onFollowClick = { isFollowed ->
                    followAudioClass(it.id, isFollowed)
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    audioClass: UserAudioClass,
    onFollowClick: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),

            ) {
            Text(
                text = audioClass.name, style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            MultiChoiceSegmentedButtonRow {
                SegmentedButton(
                    checked = audioClass.isFollowed,
                    onCheckedChange = { onFollowClick(it) },
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = if (audioClass.isFollowed) "Unfollow" else "Follow"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                audioClass.ancestors.forEach {
                    MaeTag(it)
                }
            }
        }
    }
}


@Preview
@Composable
fun CategoryCardPreview() {
    CategoryCard(
        UserAudioClass(
            id = "1",
            name = "Category",
            ancestors = listOf(
                "Ancestor 1", "Ancestor 2", "Ancestor 3", "Ancestor 4", "Ancestor 5", "Ancestor 6"
            ),
            isFollowed = false
        ),
        onFollowClick = {}
    )
}