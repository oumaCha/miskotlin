package com.patrest.miskotlin

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room


class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var viewModel: MediaViewModel

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectImage(this, it.toString())
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "media-database").build()

        viewModel = ViewModelProvider(this, ViewModelFactory(db.mediaItemDao()))[MediaViewModel::class.java]
        setContent {
            MediaApp(viewModel, onImageSelect = { pickImageLauncher.launch("image/*") })
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaApp(viewModel: MediaViewModel, onImageSelect: () -> Unit) {
    val selectedMediaItem by viewModel.selectedItem.collectAsState()

    Scaffold(
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (selectedMediaItem != null) {
                    MediaReadView(
                        mediaItem = selectedMediaItem!!,
                        onMenuClick = { /* Menu action */ },
                        onBack = { viewModel.selectMediaItem(null) },
                        onDelete = { viewModel.deleteItem(selectedMediaItem) }
                    )
                } else {
                    MediaItemList(
                        viewModel = viewModel,
                        onItemSelected = { item -> viewModel.selectMediaItem(item) },
                        onImageSelect = onImageSelect
                    )
                }
            }
        }
    )
}

