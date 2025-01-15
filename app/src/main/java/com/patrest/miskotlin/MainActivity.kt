
package com.patrest.miskotlin


import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.util.Log

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: AppDatabase
    private lateinit var viewModel: MediaViewModel


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            viewModel.selectImage(this, imageUri.toString())
        }
    }

    private fun handleSave(title: String, imagePath: String?) {
        if (checkLocationPermission()) {
            getLastKnownLocation { location ->
                val lat = location?.latitude ?: 52.545995
                val lng = location?.longitude ?: 13.351148
                viewModel.addNewItem(title, imagePath, lat, lng)
                viewModel.clearSelectedImagePath()
            }
        } else {
            requestLocationPermission()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "media-database"
        ).addMigrations(MIGRATION_1_2).build()

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(db.mediaItemDao())
        )[MediaViewModel::class.java]

        lifecycleScope.launch {
            viewModel.mediaItems.collectLatest { mediaList ->
                Log.d("MainActivity", "Loaded Media Items: $mediaList")
            }
        }


        setContent {
            val navController = rememberNavController()

            var isSideMenuVisible by remember { mutableStateOf(false) }

            MediaApp(
                viewModel = viewModel,
                isSideMenuVisible = isSideMenuVisible,
                onSideMenuToggle = { isSideMenuVisible = !isSideMenuVisible },
                onImageSelect = { pickImageLauncher.launch("image/*") },
                navController = navController,
                onSaveMediaItem = { title, imagePath -> handleSave(title, imagePath) }
            )
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun getLastKnownLocation(onLocationReceived: (Location?) -> Unit) {
        try {
            if (checkLocationPermission()) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        onLocationReceived(location)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MainActivity", "Error obtaining location: ${exception.message}")
                        onLocationReceived(null)
                    }
            } else {
                Log.e("MainActivity", "Location permission not granted")
                onLocationReceived(null)
            }
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Location permission not granted")
            onLocationReceived(null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaApp(
    viewModel: MediaViewModel,
    isSideMenuVisible: Boolean,
    onSideMenuToggle: () -> Unit,
    onImageSelect: () -> Unit,
    navController: NavHostController,
    onSaveMediaItem: (String, String?) -> Unit,
) {
    // val selectedMediaItem by viewModel.selectedItem.collectAsState()
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
                NavHost(
                    navController = navController,
                    startDestination = "medien",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("medien") {
                        MediaItemList(
                            viewModel = viewModel,
                            navController = navController,
                            onImageSelect = onImageSelect,
                            onSideMenuToggle = onSideMenuToggle,
                            onSaveMediaItem = onSaveMediaItem
                        )
                    }
                    composable("karte") {
                        val mediaItems by viewModel.mediaItems.collectAsState()
                        MapScreen(
                            mediaItems = mediaItems,
                            onMenuClick = onSideMenuToggle
                        )
                    }

                    composable("readview/{itemId}") { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()
                        MediaReadView(
                            itemId = itemId,
                            viewModel = viewModel,
                            onMenuClick = onSideMenuToggle,
                            onDelete = {
                                itemId?.let {
                                    val item = viewModel.mediaItems.value.find { mediaItem -> mediaItem.id.toInt() == itemId }
                                    if (item != null) {
                                        viewModel.requestDeleteConfirmation(item)
                                    }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                if (isSideMenuVisible) {
                    SideMenu(
                        navController = navController,
                        onMenuItemClick = { menuItem ->
                            when (menuItem) {
                                "karte" -> navController.navigate("karte")
                                "medien" -> navController.navigate("medien")
                            }
                            onSideMenuToggle()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .fillMaxHeight()
                            .background(Color.DarkGray)
                    )
                }
            }
        }
    )
}