package com.mural.devifeed.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mural.devifeed.model.FeedPost

@Database(
    entities = [FeedPost::class],
    version = 2,
    exportSchema = false
)
abstract class DevifeedDatabase : RoomDatabase() {
    companion object {
        fun create(context: Context): DevifeedDatabase {
            return Room.databaseBuilder(context, DevifeedDatabase::class.java, "devifeed.db")
                .fallbackToDestructiveMigration() //TODO ok on test projects
                .build()
        }
    }

    abstract fun getFeedDao(): FeedPostDao
}