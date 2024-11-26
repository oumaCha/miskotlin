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

    @Update
    suspend fun update(mediaItem: MediaItem)  // Update an existing item
    @Delete
    suspend fun delete(mediaItem: MediaItem)
}
