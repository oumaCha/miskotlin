package com.patrest.miskotlin

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.patrest.miskotlin.data.AppDatabase
import com.patrest.miskotlin.data.MIGRATION_1_2
import com.patrest.miskotlin.data.MediaItem
import com.patrest.miskotlin.utils.PermissionUtils
import com.patrest.miskotlin.utils.PermissionUtils.getLastKnownLocation
import com.patrest.miskotlin.viewmodel.MediaViewModel
import com.patrest.miskotlin.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: AppDatabase
    private lateinit var viewModel: MediaViewModel

    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    // pickImageLauncher öffnet die Galerie. Das wird in der MediaApp mit onImageSelect = { pickImageLauncher.launch("image/*") } weitergegeben.
    // Nach der Auswahl wird die URI ans ViewModel übergeben,
    // das das Bild kopiert und den Pfad der Kopie speichert.
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            viewModel.selectImage(this, imageUri)
        }
    }

    // handleSave entscheidet, welche Funktion im ViewModel aufgerufen wird.
    // Wenn es sich um ein neues Medium handelt, wird es erstellt, und wenn es ein bestehendes Medium ist,
    // wird es aktualisiert. Die Daten werden dann in der Datenbank gespeichert.

    private fun handleSave(title: String, imagePath: String?, mediaItemToEdit: MediaItem? = null) {
        if (PermissionUtils.checkLocationPermission(this)) {
            getLastKnownLocation(this) { location ->
                val deviceLatitude = location?.latitude ?: 52.545995
                val deviceLongitude = location?.longitude ?: 13.351148

                val imageLatitude = viewModel.selectedImageLocation.value?.first
                val imageLongitude = viewModel.selectedImageLocation.value?.second

                if (mediaItemToEdit != null) {
                    val updatedItem = mediaItemToEdit.copy(
                        title = title,
                        source = imagePath ?: mediaItemToEdit.source,
                        latitude = imageLatitude ?: mediaItemToEdit.latitude,
                        longitude = imageLongitude ?: mediaItemToEdit.longitude
                    )
                    viewModel.saveEditedItem(updatedItem)
                } else {
                    viewModel.addNewItem(
                        title,
                        imagePath,
                        latitude = imageLatitude ?: deviceLatitude,
                        longitude = imageLongitude ?: deviceLongitude
                    )
                }

                viewModel.clearSelectedImagePath()
            }
        } else {
            PermissionUtils.requestLocationPermission(this)
        }
    }


    private fun setupContent(deviceLocation: LatLng) {
        setContent {
            val navController = rememberNavController()

            var isSideMenuVisible by remember { mutableStateOf(false) }

            MediaApp(
                viewModel = viewModel,
                isSideMenuVisible = isSideMenuVisible,
                onSideMenuToggle = { isSideMenuVisible = !isSideMenuVisible },
                onImageSelect = { pickImageLauncher.launch("image/*") },
                navController = navController,
                onSaveMediaItem = { title, imagePath, mediaItemToEdit ->
                    handleSave(title, imagePath, mediaItemToEdit)
                },
                deviceLocation = deviceLocation
            )
        }
    }

    private fun fetchLastKnownLocation() {
        getLastKnownLocation(this) { location ->
            if (location != null) {
                Log.d(
                    "MainActivity",
                    "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                )
            } else {
                Toast.makeText(this, "Failed to fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!PermissionUtils.checkLocationPermission(this)) {
            PermissionUtils.requestLocationPermission(this)
            return
        }

        requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val readImagesGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            val locationGranted = permissions[Manifest.permission.ACCESS_MEDIA_LOCATION] == true

            if (readImagesGranted && locationGranted) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

        requestMediaPermissions()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "media-database"
        ).addMigrations(MIGRATION_1_2).build()

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(db.mediaItemDao())
        )[MediaViewModel::class.java]

        PermissionUtils.requestLocationPermission(
            activity = this,
            onPermissionGranted = { fetchLastKnownLocation() },
            onPermissionDenied = { Log.e("MainActivity", "Location permission denied") }
        )

        getLastKnownLocation(this) { location ->
            val deviceLocation = location?.let { LatLng(it.latitude, it.longitude) }
                ?: LatLng(52.5463, 13.3547)

            setupContent(deviceLocation)
        }
    }

    private fun requestMediaPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        }
    }
}
