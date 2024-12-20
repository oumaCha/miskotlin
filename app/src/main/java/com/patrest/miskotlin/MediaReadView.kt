package com.patrest.miskotlin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.tooling.preview.Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaReadView(
    mediaItem: MediaItem,
    onMenuClick: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
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
                            painter = painterResource(id = R.drawable.ic_return_foreground), // Ícone de voltar
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

@Preview(showBackground = true)
@Composable
fun MediaReadViewPreview() {
    val sampleMediaItem = MediaItem(
        id = 1,
        title = "Example Media",
        source = "https://picsum.photos/300/300",
        createdDate = System.currentTimeMillis()
    )
    MediaReadView(
        mediaItem = sampleMediaItem,
        onMenuClick = {},
        onBack = {},
        onDelete = {}
    )
}
