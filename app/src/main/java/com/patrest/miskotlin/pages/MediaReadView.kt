package com.patrest.miskotlin.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.patrest.miskotlin.R
import com.patrest.miskotlin.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaReadView(
    itemId: Int?,
    viewModel: MediaViewModel,
    onMenuClick: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {

    val mediaItem = itemId?.let { id -> viewModel.mediaItems.value.firstOrNull { it.id.toInt() == id } }


    if (mediaItem == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "MediaItem nicht gefunden", color = Color.Black)
        }
        return
    }

    Box {
        Scaffold(
            containerColor = Color.Black,
            topBar = {
                TopAppBar(
                    title = { Text(mediaItem.title, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sandwich),
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onDelete) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete_white),
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2E2E2E))
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = Color(0xFF2E2E2E),
                    content = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_return_foreground),
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(mediaItem.source),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        )
    }
}



