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
    onItemSelected: (MediaItem) -> Unit
) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    val showActionMenu by viewModel.showActionMenu.collectAsState()
    val actionMenuItem by viewModel.actionMenuItem.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()

    Scaffold(
        topBar = {
            MediaTopBar(onAddClick = { viewModel.addNewItem() })
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
                            onItemClick = { onItemSelected(item) }, // ReadView
                            onOptionsClick = { viewModel.openActionMenuDialog(item) } // Dialog
                        )
                    }
                }

                if (showActionMenu && actionMenuItem != null) {
                    ActionMenuDialog(
                        item = actionMenuItem!!,
                        onDismiss = { viewModel.closeDialogs() },
                        onDelete = { viewModel.deleteItem(actionMenuItem) },
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

                // Espaçamento entre a imagem e o texto
                Spacer(modifier = Modifier.width(8.dp))

                // Título e Data
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

            // Ícone "Options" no lado direito
            IconButton(onClick = onOptionsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_options),
                    contentDescription = "Options",
                    tint = Color.White
                )
            }
        }

        // Linha Separadora
        HorizontalDivider(
            color = Color.Gray,
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaTopBar(onAddClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "Medien", color = Color.White) },
        navigationIcon = {
            IconButton(onClick = { }) {
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


