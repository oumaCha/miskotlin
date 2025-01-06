package com.patrest.miskotlin

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface MediaItemDao {
    @Insert
    suspend fun insert(mediaItem: MediaItem)

    @Query("SELECT * FROM media_items")
    suspend fun getAllMediaItems(): List<MediaItem>

    @Query("SELECT * FROM media_items WHERE latitude IS NOT NULL AND longitude IS NOT NULL")
    suspend fun getMediaItemsWithLocation(): List<MediaItem> // Fetch items with valid locations

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaItemById(id: Long): MediaItem? // Fetch a specific item by its ID

    @Update
    suspend fun update(mediaItem: MediaItem)
    @Delete
    suspend fun delete(mediaItem: MediaItem)


}
