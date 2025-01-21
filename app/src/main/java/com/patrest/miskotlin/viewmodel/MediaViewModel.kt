package com.patrest.miskotlin.viewmodel

import android.content.Context
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrest.miskotlin.data.MediaItem
import com.patrest.miskotlin.data.MediaItemDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.GpsDirectory
import com.drew.metadata.Directory
import java.io.InputStream
import com.drew.metadata.Metadata
import com.drew.metadata.Tag


class MediaViewModel(private val dao: MediaItemDao) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> get() = _mediaItems

    val mediaItemsWithLocation: StateFlow<List<MediaItem>> = _mediaItems.map { list ->
        list.filter { it.latitude != null && it.longitude != null && it.latitude != 0.0 && it.longitude != 0.0 }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _actionMenuItem = MutableStateFlow<MediaItem?>(null)
    val actionMenuItem: StateFlow<MediaItem?> get() = _actionMenuItem

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> get() = _showEditDialog

    private val _showActionMenu = MutableStateFlow(false)
    val showActionMenu: StateFlow<Boolean> get() = _showActionMenu

    private val _titleCounter = MutableStateFlow(1)
    val titleCounter: StateFlow<Int> get() = _titleCounter

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> get() = _showCreateDialog

    private val _selectedImagePath = MutableStateFlow<String?>(null)
    val selectedImagePath: StateFlow<String?> get() = _selectedImagePath

    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> get() = _showDeleteConfirmDialog

    private val _itemToDelete = MutableStateFlow<MediaItem?>(null)
    val itemToDelete: StateFlow<MediaItem?> get() = _itemToDelete

    private val _selectedImageLocation = MutableStateFlow<Pair<Double?, Double?>?>(null)
    val selectedImageLocation: StateFlow<Pair<Double?, Double?>?> get() = _selectedImageLocation


    init {
        loadMediaItems()
    }

    fun loadMediaItems() {
        viewModelScope.launch {
            try {
                val items = dao.getAllMediaItems()
                _mediaItems.value = items.map { mediaItem ->
                    mediaItem.copy(
                        latitude = mediaItem.latitude ?: 52.545995, // Default latitude
                        longitude = mediaItem.longitude ?: 13.351148 // Default longitude
                    )
                }
                _titleCounter.value = _mediaItems.value.size + 1
                Log.d("ViewModel", "Loaded media items: ${_mediaItems.value}")
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to load media items: ${e.message}", e)
            }
        }
    }

    fun extractLocationFromImage2(imagePath: String): Pair<Double, Double>? {
        return try {
            val exif = ExifInterface(imagePath)
            val latitude = exif.latLong?.getOrNull(0)
            val longitude = exif.latLong?.getOrNull(1)

            Log.d("extractLocationFromImage", "Exif Data: ${exif.latLong}")
            Log.d("extractLocationFromImage", "Latitude: $latitude, Longitude: $longitude")


            if (latitude != null && longitude != null) {
                latitude to longitude
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Error extracting location from image: ${e.message}")
            null
        }
    }
    fun extractLocationFromImage(context: Context, imageUri: Uri): Pair<Double?, Double?>? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                val metadata = ImageMetadataReader.readMetadata(inputStream)
                val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)

                if (gpsDirectory != null) {
                    val location = gpsDirectory.geoLocation
                    if (location != null) {
                        Log.d("MetadataExtractor", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                        return location.latitude to location.longitude
                    } else {
                        Log.w("MetadataExtractor", "No geolocation found in image metadata.")
                    }
                } else {
                    Log.w("MetadataExtractor", "No GPS Directory found in image metadata.")
                }
            }
            null
        } catch (e: Exception) {
            Log.e("MetadataExtractor", "Error extracting metadata: ${e.message}", e)
            null
        }
    }
    fun logAllMetadata(filePath: String) {
        try {
            val file = File(filePath)
            val metadata: Metadata = ImageMetadataReader.readMetadata(file)

            for (directory: Directory in metadata.directories) {
                Log.d("MetadataExtractor", "Directory: ${directory.name}")
                for (tag: Tag in directory.tags) {
                    Log.d("MetadataExtractor", "Tag: ${tag.tagName}, Value: ${tag.description}")
                }
                if (directory.hasErrors()) {
                    for (error in directory.errors) {
                        Log.e("MetadataExtractor", "Error in directory ${directory.name}: $error")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MetadataExtractor", "Error reading metadata: ${e.message}", e)
        }
    }
    fun addNewItem(
        title: String,
        imagePath: String? = null,
        latitude: Double = 52.545995,
        longitude: Double = 13.351148
    ) {
        viewModelScope.launch {
            val size = (100..300).random()
            val finalPath = imagePath ?: "https://picsum.photos/$size/$size"

            Log.d("addNewItem", "Image Path: $imagePath")
            Log.d("addNewItem", "Final Path: $finalPath")


            val imageLocation = null

            val location = imageLocation ?: (latitude to longitude)

            val newItem = MediaItem(
                title = title,
                source = finalPath,
                createdDate = System.currentTimeMillis(),
                latitude = location.first,
                longitude = location.second
            )
            dao.insert(newItem)
            loadMediaItems()
        }
    }
    fun selectImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)
                val file = File(context.filesDir, "selected_image_${System.currentTimeMillis()}.jpg")

                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                logAllMetadata(file.absolutePath)
                val location = extractLocationFromImage(context, Uri.fromFile(file))
                val latitude = location?.first
                val longitude = location?.second

                if (latitude != null && longitude != null) {
                    Log.d("selectImage", "Extracted Location - Latitude: $latitude, Longitude: $longitude")
                } else {
                    Log.w("selectImage", "Image does not contain valid location data")
                }

                _selectedImagePath.value = file.absolutePath
                _selectedImageLocation.value = latitude to longitude

            } catch (e: Exception) {
                Log.e("ViewModel", "Error selecting image: ${e.message}", e)
            }
        }
    }
    fun clearSelectedImagePath() {
        _selectedImagePath.value = null
    }

    fun openActionMenuDialog(item: MediaItem) {
        _actionMenuItem.value = item
        _showActionMenu.value = true
    }

    fun closeDialogs() {
        _showActionMenu.value = false
        _showEditDialog.value = false
    }

    fun requestDeleteConfirmation(item: MediaItem) {
        _actionMenuItem.value = null
        _showActionMenu.value = false
        _itemToDelete.value = item
        _showDeleteConfirmDialog.value = true
    }

    fun confirmDelete(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val item = _itemToDelete.value
            if (item != null) {
                try {
                    dao.delete(item)
                    _mediaItems.value = _mediaItems.value.filterNot { it.id == item.id }
                    Log.d("ViewModel", "Item deleted: ${item.title}")
                    onResult(true)
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error deleting item: ${e.message}", e)
                    onResult(false)
                } finally {
                    _showDeleteConfirmDialog.value = false
                    _itemToDelete.value = null
                }
            } else {
                onResult(false)
            }
        }
    }


    fun saveEditedItem(updatedItem: MediaItem) {
        viewModelScope.launch {
            try {
                dao.update(updatedItem)
                _mediaItems.value = _mediaItems.value.map { if (it.id == updatedItem.id) updatedItem else it }
                closeDialogs()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error saving edited item: ${e.message}", e)
            }
        }
    }

    fun openEditDialog() {
        _showActionMenu.value = false
        _showEditDialog.value = true
    }

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmDialog.value = false
        _itemToDelete.value = null
    }

    fun openCreateDialog() {
        _showCreateDialog.value = true
    }

    fun closeCreateDialog() {
        _showCreateDialog.value = false
    }
}
