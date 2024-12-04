package com.patrest.miskotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.*
import coil.compose.rememberAsyncImagePainter


// test




class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "media-database").build()


        setContent {
            MediaApp(db)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaApp(db: AppDatabase) {
    val scope = rememberCoroutineScope()
    var mediaItems by remember { mutableStateOf(emptyList<MediaItem>()) }
    var titleCounter by remember { mutableIntStateOf(1) }
    var selectedMediaItem by remember { mutableStateOf<MediaItem?>(null) }

    // Carregar dados do banco de dados ao inicializar
    LaunchedEffect(Unit) {
        scope.launch {
            mediaItems = db.mediaItemDao().getAllMediaItems()
            titleCounter = mediaItems.size + 1
        }
    }

    Scaffold(
        content = { innerPadding ->
            if (selectedMediaItem != null) {
                // Renderiza apenas o MediaReadView
                MediaReadView(
                    mediaItem = selectedMediaItem!!,
                    onMenuClick = { /* Menu action */ },
                    onBack = { selectedMediaItem = null },
                    onDelete = {
                        scope.launch {
                            db.mediaItemDao().delete(selectedMediaItem!!)
                            mediaItems = db.mediaItemDao().getAllMediaItems()
                            selectedMediaItem = null
                        }
                    }
                )
            } else {
                // Renderiza apenas o MediaItemList
                MediaItemList(
                    items = mediaItems,
                    modifier = Modifier.padding(innerPadding),
                    db = db,
                    onListChanged = { updatedItems -> mediaItems = updatedItems },
                    onItemSelected = { item -> selectedMediaItem = item },
                    titleCounter = titleCounter,
                    incrementTitleCounter = { titleCounter++ }
                )
            }
        }
    )
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaItemList(
    items: List<MediaItem>,
    modifier: Modifier = Modifier,
    db: AppDatabase,
    onListChanged: (List<MediaItem>) -> Unit,
    onItemSelected: (MediaItem) -> Unit,
    titleCounter: Int,
    incrementTitleCounter: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf<MediaItem?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MediaTopBar(
                onAddClick = {
                    scope.launch {
                        val size = (100..300).random()
                        val newMediaItem = MediaItem(
                            title = "Media Item $titleCounter",
                            source = "https://picsum.photos/$size/$size",
                            createdDate = Date().time
                        )
                        db.mediaItemDao().insert(newMediaItem)
                        onListChanged(db.mediaItemDao().getAllMediaItems())
                        incrementTitleCounter()
                    }
                }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(modifier = modifier.fillMaxSize().background(Color.DarkGray).padding(8.dp)) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemSelected(item) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                Image(
                                    painter = rememberAsyncImagePainter(item.source),
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = SimpleDateFormat(
                                            "dd.MM.yyyy",
                                            Locale.getDefault()
                                        ).format(item.createdDate),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                            IconButton(onClick = {
                                selectedItem = item
                                showDialog = true
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_options),
                                    contentDescription = "Options",
                                    tint = Color.White
                                )
                            }
                        }
                        HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
                    }
                }

                // Show action menu dialog
                if (showDialog && selectedItem != null) {
                    ActionMenuDialog(
                        item = selectedItem!!,
                        onDismiss = { showDialog = false },
                        onDelete = {
                            selectedItem?.let { itemToDelete ->
                                scope.launch {
                                    db.mediaItemDao().delete(itemToDelete)
                                    val updatedItems = db.mediaItemDao().getAllMediaItems()
                                    onListChanged(updatedItems)
                                }
                            }
                            showDialog = false
                        },
                        onEdit = {
                            showDialog = false
                            showEditDialog = true
                        }
                    )
                }

                // Show edit media item dialog
                if (showEditDialog && selectedItem != null) {
                    EditMediaItemDialog(
                        item = selectedItem!!,
                        onDismiss = { showEditDialog = false },
                        onSave = { updatedItem ->
                            scope.launch {
                                db.mediaItemDao().update(updatedItem)
                                val updatedItems = db.mediaItemDao().getAllMediaItems()
                                onListChanged(updatedItems)
                            }
                            showEditDialog = false
                        }
                    )
                }
            }
        }
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
                // Save the updated media item
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


    @Preview(showBackground = true)
    @Composable
    fun MediaItemListPreview() {
        // MediaApp()
    }
