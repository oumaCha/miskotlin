package com.patrest.miskotlin.pages

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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.patrest.miskotlin.R
import com.patrest.miskotlin.components.ActionMenuDialog
import com.patrest.miskotlin.components.MediaItemDialog
import com.patrest.miskotlin.data.FilterType
import com.patrest.miskotlin.data.MediaItem
import com.patrest.miskotlin.viewmodel.MediaViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaItemList(
    viewModel: MediaViewModel,
    navController: NavHostController,
    onImageSelect: () -> Unit,
    onSideMenuToggle: () -> Unit,
    onSaveMediaItem: (String, String?, MediaItem?, Boolean) -> Unit
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
        bottomBar = {
            MediaBottomBar(viewModel = viewModel)
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
                            onItemClick = { navController.navigate("readview/${item.id}") },
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

                if (showEditDialog && actionMenuItem != null  || showCreateDialog) {
                    MediaItemDialog(
                        defaultTitle = if (showEditDialog) actionMenuItem!!.title else "Media Item $titleCounter",
                        mediaItem = if (showEditDialog) actionMenuItem else null,
                        onDismiss = {
                            if (showEditDialog) viewModel.closeDialogs() else viewModel.closeCreateDialog()
                        },
                        onSave = onSaveMediaItem,
                        onDelete = {  viewModel.requestDeleteConfirmation(actionMenuItem!!) },
                        onImageClick = onImageSelect,
                        selectedImagePath = viewModel.selectedImagePath.collectAsState().value,
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

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    painter = painterResource(
                        id = if (item.isRemote) R.drawable.ic_cloud else R.drawable.ic_device
                    ),
                    contentDescription = if (item.isRemote) "Remote" else "Local",
                    modifier = Modifier.size(20.dp)
                )
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
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onMenuClick() }
                    .padding(8.dp)
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
fun MediaBottomBar(viewModel: MediaViewModel) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    BottomAppBar(
        containerColor = Color(0xFF2E2E2E),
        modifier = Modifier.height(48.dp),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = { viewModel.filterMediaItems(FilterType.LOCAL) },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = if (selectedFilter == FilterType.LOCAL) Color.Black else Color.White
                    )
                ) {
                    Text("Lokal")
                }


                TextButton(
                    onClick = { viewModel.filterMediaItems(FilterType.REMOTE) },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = if (selectedFilter == FilterType.REMOTE) Color.Black else Color.White
                    )
                ) {
                    Text("Remote")
                }

                TextButton(
                    onClick = { viewModel.filterMediaItems(FilterType.ALL) },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = if (selectedFilter == FilterType.ALL) Color.Black else Color.White
                    )
                ) {
                    Text("Alle")
                }
            }
        }
    )
}





