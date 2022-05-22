package com.delivery.sopo.data.database.room.dao

import androidx.room.*
import com.delivery.sopo.data.database.room.entity.AuthTokenEntity

@Dao
interface AuthTokenDao
{
    @Query("SELECT * FROM AUTH_TOKEN LIMIT 1")
    fun get() : AuthTokenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(authToken : AuthTokenEntity)

    @Update
    fun update(authToken : AuthTokenEntity)

    @Delete
    fun delete(authToken : AuthTokenEntity)

    @Query("DELETE FROM AUTH_TOKEN")
    fun getAndDrop()
}