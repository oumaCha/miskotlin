package com.patrest.miskotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val source: String,
    val createdDate: Long = System.currentTimeMillis(),
    val latitude: Double?,
    val longitude: Double?
)
