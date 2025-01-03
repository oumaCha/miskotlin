package com.patrest.miskotlin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class MediaViewModel(private val dao: MediaItemDao) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> get() = _mediaItems

    private val _selectedItem = MutableStateFlow<MediaItem?>(null) // Para o ReadView
    val selectedItem: StateFlow<MediaItem?> get() = _selectedItem

    private val _actionMenuItem = MutableStateFlow<MediaItem?>(null) // Para o Dialog
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



    init {
        loadMediaItems()
    }

    fun loadMediaItems() {
        viewModelScope.launch {
            _mediaItems.value = dao.getAllMediaItems()
            _titleCounter.value = _mediaItems.value.size + 1
        }
    }

    fun selectImage(context: Context, imageUri: String) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(Uri.parse(imageUri))
                val file = File(context.filesDir, "selected_image_${System.currentTimeMillis()}.jpg")

                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Salva o caminho absoluto
                _selectedImagePath.value = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun addNewItem(
        title: String = "Media Item ${_titleCounter.value}",
        imagePath: String? = null
    ) {
        viewModelScope.launch {
            val size = (100..300).random()
            val finalPath = imagePath ?: "https://picsum.photos/$size/$size"
            val newItem = MediaItem(
                title = title,
                source = finalPath,
                createdDate = System.currentTimeMillis()
            )
            dao.insert(newItem)
            loadMediaItems()
        }
    }



    // ReadView
    fun selectMediaItem(item: MediaItem?) {
        _selectedItem.value = item
    }

    // ActionMenuDialog
    fun openActionMenuDialog(item: MediaItem) {
        _actionMenuItem.value = item
        _showActionMenu.value = true
    }


    fun requestDeleteConfirmation(item: MediaItem) {
        _showActionMenu.value = false
        _actionMenuItem.value = null

        _itemToDelete.value = item
        _showDeleteConfirmDialog.value = true
    }


    fun confirmDelete() {
        viewModelScope.launch {
            _itemToDelete.value?.let { item ->
                dao.delete(item)
                loadMediaItems()
            }
            _showDeleteConfirmDialog.value = false
            _itemToDelete.value = null
        }
    }

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmDialog.value = false
        _itemToDelete.value = null
    }

    fun openEditDialog() {
        _showActionMenu.value = false
        _showEditDialog.value = true
    }

    fun closeDialogs() {
        _showEditDialog.value = false
        _showActionMenu.value = false
    }

    fun saveEditedItem(updatedItem: MediaItem) {
        viewModelScope.launch {
            dao.update(updatedItem)
            loadMediaItems()
            closeDialogs()
        }
    }

    fun openCreateDialog() {
        _showCreateDialog.value = true
    }

    fun closeCreateDialog() {
        _showCreateDialog.value = false
    }
}
