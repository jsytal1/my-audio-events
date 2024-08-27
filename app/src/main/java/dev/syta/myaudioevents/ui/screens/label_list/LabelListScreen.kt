package dev.syta.myaudioevents.ui.screens.label_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.syta.myaudioevents.R
import dev.syta.myaudioevents.data.model.Label
import dev.syta.myaudioevents.designsystem.icon.MaeIcons
import dev.syta.myaudioevents.ui.MaeSharedViewModel

@Composable
fun LabelListScreen(
    viewModel: LabelListViewModel = hiltViewModel(),
    sharedViewModel: MaeSharedViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        sharedViewModel.showFab(
            icon = MaeIcons.Add,
            contentDescriptionId = R.string.add_label,
            onClick = {
                viewModel.showAddLabelDialog()
            },
        )
    }

    LabelsScreenContent(
        uiState = uiState,
        viewModel = viewModel,
    )
}

@Composable
fun AddLabelDialog(onDismiss: () -> Unit, onConfirm: (value: String) -> Unit) {
    var labelNameState by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Add Label")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = labelNameState,
                    onValueChange = { labelNameState = it },
                    label = { Text("Label Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(labelNameState)
                    onDismiss()
                }
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(text = "Cancel")
            }
        },
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun LabelList(labelList: List<Label>) {
    LazyColumn {
        items(labelList) { label ->
            ListItem(
                name = label.name,
            )
        }
    }
}

@Composable
fun ListItem(name: String) {
    Card(
        modifier = Modifier.padding(8.dp)
    ) {
        Row {
            Text(
                modifier = Modifier.weight(1f),
                text = name
            )
        }
    }
}

@Composable
fun LabelsScreenContent(
    uiState: LabelListViewModel.LabelListScreenUiState,
    viewModel: LabelListViewModel
) {
    when (uiState) {
        LabelListViewModel.LabelListScreenUiState.Loading -> {
            Text(text = "Loading")
        }

        is LabelListViewModel.LabelListScreenUiState.Ready -> {
            LabelList(
                labelList = uiState.list,
            )
            if (uiState.showAddLabelDialog) {
                AddLabelDialog(
                    onDismiss = viewModel::hideAddLabelDialog,
                    onConfirm = { labelName ->
                        viewModel.addLabel(labelName)
                        viewModel.hideAddLabelDialog()
                    }
                )
            }
        }
    }
}