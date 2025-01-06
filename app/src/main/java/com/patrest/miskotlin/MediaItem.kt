package com.patrest.miskotlin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val source: String,
    val createdDate: Long = System.currentTimeMillis(),
    val latitude: Double? = null,  // Latitude of the location, nullable if not available
    val longitude: Double? = null  // Longitude of the location, nullable if not available
)
