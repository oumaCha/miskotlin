package com.patrest.miskotlin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    init {
        loadMediaItems()
    }

    fun loadMediaItems() {
        viewModelScope.launch {
            _mediaItems.value = dao.getAllMediaItems()
            _titleCounter.value = _mediaItems.value.size + 1
        }
    }

    fun addNewItem() {
        viewModelScope.launch {
            val size = (100..300).random()
            val newItem = MediaItem(
                title = "Media Item ${_titleCounter.value}",
                source = "https://picsum.photos/$size/$size",
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

    fun deleteItem(item: MediaItem?) {
        viewModelScope.launch {
            item?.let { dao.delete(it) }
            loadMediaItems()
            closeDialogs()
            _selectedItem.value = null
        }
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
}
