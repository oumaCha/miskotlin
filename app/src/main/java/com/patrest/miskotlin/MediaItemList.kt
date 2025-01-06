package com.patrest.miskotlin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaItemList(
    viewModel: MediaViewModel,
    onItemSelected: (MediaItem) -> Unit,
    onImageSelect: () -> Unit,
    onSideMenuToggle: () -> Unit
) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    val showActionMenu by viewModel.showActionMenu.collectAsState()
    val actionMenuItem by viewModel.actionMenuItem.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    val titleCounter by viewModel.titleCounter.collectAsState()


    Scaffold(
        topBar = {
            MediaTopBar(onAddClick = { viewModel.openCreateDialog() }, onMenuClick = onSideMenuToggle)
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray)
                        .padding(8.dp)
                ) {
                    items(mediaItems) { item ->
                        MediaItemRow(
                            item = item,
                            onItemClick = { onItemSelected(item) },
                            onOptionsClick = { viewModel.openActionMenuDialog(item) }
                        )
                    }
                }

                if (showActionMenu && actionMenuItem != null) {
                    ActionMenuDialog(
                        item = actionMenuItem!!,
                        onDismiss = { viewModel.closeDialogs() },
                        onDelete = {  viewModel.requestDeleteConfirmation(actionMenuItem!!) },
                        onEdit = { viewModel.openEditDialog() }
                    )
                }

                if (showEditDialog && actionMenuItem != null) {
                    EditMediaItemDialog(
                        item = actionMenuItem!!,
                        onDismiss = { viewModel.closeDialogs() },
                        onSave = { updatedItem -> viewModel.saveEditedItem(updatedItem) }
                    )
                }

                if (showCreateDialog) {
                    CreateMediaItemDialog(
                        defaultTitle = "Media Item $titleCounter",
                        onDismiss = { viewModel.closeCreateDialog() },
                        onSave = { title, imagePath -> viewModel.addNewItem(title, imagePath) },
                        onImageClick = onImageSelect,
                        selectedImagePath = viewModel.selectedImagePath.collectAsState().value
                    )
                }
            }
        }
    )
}




@Composable
fun MediaItemRow(
    item: MediaItem,
    onItemClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                // Imagem à esquerda
                Image(
                    painter = rememberAsyncImagePainter(item.source),
                    contentDescription = "Media Item Image",
                    modifier = Modifier
                        .size(50.dp)
                        .padding(end = 8.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(item.createdDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onOptionsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_options),
                    contentDescription = "Options",
                    tint = Color.White
                )
            }
        }

        HorizontalDivider(
            color = Color.Gray,
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaTopBar(onAddClick: () -> Unit, onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "Medien", color = Color.White) },
        navigationIcon = {
            // Wrap the IconButton in a clickable modifier to make the whole area clickable
            Box(
                modifier = Modifier
                    .padding(start = 8.dp) // Adjust padding if needed
                    .clickable { onMenuClick() } // Make the whole area clickable
                    .padding(8.dp) // Add padding for spacing around the icon
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sandwich),
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onAddClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF2E2E2E)
        )
    )
}


@Composable
fun ActionMenuDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Actions for ${item.title}")
        },
        text = {
            Column {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun EditMediaItemDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onSave: (MediaItem) -> Unit
) {
    var editedTitle by remember { mutableStateOf(item.title) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Media Item") },
        text = {
            Column {
                TextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text("Title") }
                )

            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(item.copy(title = editedTitle) )

            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateMediaItemDialog(
    defaultTitle: String,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
    onImageClick: () -> Unit,
    selectedImagePath: String?
) {
    var title by remember { mutableStateOf(defaultTitle) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neues Medium") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = title,
                        onValueChange = {
                            title = it
                            showError = it.isBlank()
                        },
                        label = { Text("Titel") },
                        isError = showError,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            onImageClick()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_camera),
                            contentDescription = "Bild auswählen"
                        )
                    }
                }

                if (showError) {
                    Text(
                        text = "Titel: Eingabe erforderlich",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedImagePath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImagePath),
                        contentDescription = "Vorschaubild",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, selectedImagePath)
                        onDismiss()
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Erstellen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color.Gray)
            }
        }
    )
}




