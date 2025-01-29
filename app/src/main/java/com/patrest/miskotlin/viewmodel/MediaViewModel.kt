package com.patrest.miskotlin.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.GpsDirectory
import com.patrest.miskotlin.data.FilterType
import com.patrest.miskotlin.data.MediaItem
import com.patrest.miskotlin.data.MediaItemDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream


class MediaViewModel(private val dao: MediaItemDao) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    // val mediaItems: StateFlow<List<MediaItem>> get() = _mediaItems
    private val _filteredMediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> get() = _filteredMediaItems


    val selectedFilter = MutableStateFlow(FilterType.ALL)


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
        filterMediaItems(FilterType.ALL)
    }

    fun loadMediaItems() {
        viewModelScope.launch {
            try {
                val items = dao.getAllMediaItems()
                _mediaItems.value = items.map { mediaItem ->
                    mediaItem.copy(
                        latitude = mediaItem.latitude ?: 52.545995,
                        longitude = mediaItem.longitude ?: 13.351148
                    )
                }
                _titleCounter.value = _mediaItems.value.size + 1
                filterMediaItems(FilterType.ALL)
                Log.d("ViewModel", "Loaded media items: ${_mediaItems.value}")
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to load media items: ${e.message}", e)
            }
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

    fun filterMediaItems(filterType: FilterType) {
        viewModelScope.launch {
            val filteredItems = when (filterType) {
                FilterType.ALL -> _mediaItems.value
                FilterType.LOCAL -> _mediaItems.value.filter { !it.isRemote }
                FilterType.REMOTE -> _mediaItems.value.filter { it.isRemote }
            }
            _filteredMediaItems.value = filteredItems
            selectedFilter.value = filterType
        }
    }

    fun addNewItem(
        title: String,
        imagePath: String? = null,
        latitude: Double = 52.545995,
        longitude: Double = 13.351148,
        isRemote: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val finalPath = imagePath ?: ""
                Log.d("addNewItem", "Title: $title, ImagePath: $imagePath, IsRemote: $isRemote")
                val newItem = MediaItem(
                    title = title,
                    source = finalPath,
                    createdDate = System.currentTimeMillis(),
                    latitude = latitude,
                    longitude = longitude,
                    isRemote = isRemote
                )
                dao.insert(newItem)
                loadMediaItems()
            } catch (e: Exception) {
                Log.e("addNewItem", "Error: ${e.message}")
            }
        }
    }


    fun saveEditedItem(updatedItem: MediaItem) {
        viewModelScope.launch {
            try {
                dao.update(updatedItem)
                _mediaItems.value = _mediaItems.value.map { if (it.id == updatedItem.id) updatedItem else it }
                // closeDialogs()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error saving edited item: ${e.message}", e)
            }
        }
    }
    // selectImage kopiert das ausgewählte Bild in den App-Speicher,
    // liest die Metadaten (z. B. Standort) und speichert den Pfad und die Koordinaten.

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

                    filterMediaItems(selectedFilter.value)

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
