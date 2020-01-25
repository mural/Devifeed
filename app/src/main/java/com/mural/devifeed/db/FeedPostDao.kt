package com.mural.devifeed.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mural.devifeed.model.FeedPost

@Dao
interface FeedPostDao {

    @Query("SELECT * FROM posts ORDER BY topIndex ASC")
    fun list(): DataSource.Factory<Int, FeedPost>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<FeedPost>)

    @Query("SELECT MAX(topIndex) + 1 FROM posts")
    fun getNextIndex(): Int

    @Query("DELETE FROM posts")
    fun deleteAll()
}