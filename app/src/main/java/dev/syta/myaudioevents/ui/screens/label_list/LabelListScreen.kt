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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
fun EditLabelDialog(
    onDismiss: () -> Unit,
    onConfirm: (value: String) -> Unit,
    name: String = "",
    title: String
) {
    var labelNameState by remember { mutableStateOf(name) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = title) },
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
fun LabelList(
    labelList: List<Label>,
    showEditLabelDialog: (Label) -> Unit,
    showDeleteLabelDialog: (Label) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(labelList) { label ->
            ListItem(
                name = label.name,
                showEditDialog = {
                    showEditLabelDialog(label)
                },
                showDeleteDialog = {
                    showDeleteLabelDialog(label)
                }
            )
        }
    }
}

@Composable
fun ListItem(name: String, showEditDialog: () -> Unit, showDeleteDialog: () -> Unit) {
    Card {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                text = name
            )
            IconButton(
                onClick = showEditDialog
            ) {
                Icon(
                    imageVector = MaeIcons.Edit,
                    contentDescription = "Edit"
                )
            }
            IconButton(
                onClick = showDeleteDialog,
            ) {
                Icon(
                    imageVector = MaeIcons.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }
}

@Preview
@Composable
fun ListItemPreview() {
    ListItem(
        name = "Label 1",
        showEditDialog = { },
        showDeleteDialog = { },
    )
}

@Preview
@Composable
fun LabelListPreview() {
    LabelList(
        labelList = listOf(
            Label("Label 1"),
            Label("Label 2"),
            Label("Label 3"),
        ),
        showEditLabelDialog = { },
        showDeleteLabelDialog = { },
    )
}


@Composable
fun ConfirmDialog(
    title: String,
    message: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = message,
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "Cancel")
            }
        },
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun ConfirmDialogPreview() {
    ConfirmDialog(
        title = "Delete",
        message = {
            Text(text = "Are you sure you want to delete this label?")
        },
        onDismiss = { },
        onConfirm = { },
    )
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
                showEditLabelDialog = viewModel::showEditLabelDialog,
                showDeleteLabelDialog = viewModel::showDeleteLabelDialog
            )
            if (uiState.showAddLabelDialog) {
                EditLabelDialog(
                    onDismiss = viewModel::hideAddLabelDialog,
                    onConfirm = { labelName ->
                        viewModel.addLabel(labelName)
                        viewModel.hideAddLabelDialog()
                    },
                    title = stringResource(R.string.add)
                )
            }
            if (uiState.showEditLabelDialog) {
                EditLabelDialog(
                    title = stringResource(R.string.edit),
                    onDismiss = viewModel::hideEditLabelDialog,
                    onConfirm = { labelName ->
                        viewModel.editLabel(
                            id = uiState.selectedLabel!!.id,
                            name = labelName
                        )
                        viewModel.hideAddLabelDialog()
                    },
                    name = uiState.selectedLabel!!.name
                )
            }
            if (uiState.showDeleteLabelDialog) {
                ConfirmDialog(
                    title = stringResource(R.string.delete),
                    message = {
                        Text(
                            text = stringResource(
                                R.string.delete_label_message,
                            ) + " " + (uiState.selectedLabel?.name ?: "")
                        )
                    },
                    onDismiss = viewModel::hideDeleteLabelDialog,
                    onConfirm = {
                        viewModel.deleteLabel(
                            id = uiState.selectedLabel!!.id,
                        )
                        viewModel.hideDeleteLabelDialog()
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun EditLabelDialogPreview() {
    EditLabelDialog(
        onDismiss = { },
        onConfirm = { },
        name = "Label 1",
        title = stringResource(R.string.edit)
    )
}