
package com.patrest.miskotlin

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


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

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "media-database").addMigrations(MIGRATION_1_2).build()

        viewModel = ViewModelProvider(this, ViewModelFactory(db.mediaItemDao()))[MediaViewModel::class.java]

        setContent {
            // Initialize NavController
            val navController = rememberNavController()

            // State to toggle the side menu visibility
            var isSideMenuVisible by remember { mutableStateOf(false) }

            // MediaApp composable
            MediaApp(
                viewModel = viewModel,
                isSideMenuVisible = isSideMenuVisible,
                onSideMenuToggle = { isSideMenuVisible = !isSideMenuVisible },
                onImageSelect = { pickImageLauncher.launch("image/*") },
                navController = navController // Pass NavController to the MediaApp
            )
        }
    }
}

@Composable
fun KarteScreen(navController: NavHostController) {
    // Here you can implement your map or content for the "Karte" screen
    Text(text = "Welcome to the Karte screen")
}

@Preview
@Composable
fun PreviewKarteScreen() {
    KarteScreen(navController = rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaApp(
    viewModel: MediaViewModel,
    isSideMenuVisible: Boolean,
    onSideMenuToggle: () -> Unit,
    onImageSelect: () -> Unit,
    navController: NavHostController // Accept NavController as a parameter
) {
    val selectedMediaItem by viewModel.selectedItem.collectAsState()
    val showDeleteDialog by viewModel.showDeleteConfirmDialog.collectAsState()
    val itemToDelete by viewModel.itemToDelete.collectAsState()

    if (showDeleteDialog) {
        val item = itemToDelete
        if (item != null) {
            DeleteConfirmDialog(
                itemTitle = item.title,
                onDismiss = { viewModel.dismissDeleteConfirmation() },
                onConfirm = { viewModel.confirmDelete() }
            )
        }
    }

    Scaffold(
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                // Navigation Host
                NavHost(
                    navController = navController,
                    startDestination = "medien", // Set initial screen to "medien"
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("medien") {
                        // Medien screen
                        MediaItemList(
                            viewModel = viewModel,
                            onItemSelected = { item -> viewModel.selectMediaItem(item) },
                            onImageSelect = onImageSelect,
                            onSideMenuToggle = onSideMenuToggle // Pass onSideMenuToggle here as well
                        )
                    }
                    composable("karte") {
                        // Karte screen
                        KarteScreen(navController) // Replace with your Karte screen composable
                    }
                }

                // Conditionally show the side menu if the state is true
                if (isSideMenuVisible) {
                    SideMenu(
                        navController = navController,
                        onMenuItemClick = { menuItem ->
                            // Navigate and dismiss the sidebar
                            when (menuItem) {
                                "medien" -> navController.navigate("medien")
                                "karte" -> navController.navigate("karte")
                            }
                            onSideMenuToggle() // Close the sidebar
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .fillMaxHeight()
                            .background(Color.DarkGray) // Apply background
                    )
                }
            }
        }
    )
}
