package com.patrest.miskotlin

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE media_items ADD COLUMN latitude REAL DEFAULT NULL")
        database.execSQL("ALTER TABLE media_items ADD COLUMN longitude REAL DEFAULT NULL")
    }
}

@Database(entities = [MediaItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao
}
