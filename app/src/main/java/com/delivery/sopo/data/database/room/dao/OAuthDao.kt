package com.delivery.sopo.data.database.room.dao

import androidx.room.*
import com.delivery.sopo.data.database.room.entity.OAuthEntity

@Dao
interface OAuthDao
{
    @Query("SELECT * FROM OAUTH WHERE user_id = :userId")
    fun get(userId : String) : OAuthEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(OAuth : OAuthEntity)

    @Update
    fun update(OAuth : OAuthEntity)

    @Delete
    fun delete(OAuth : OAuthEntity)

    @Query("DELETE FROM OAUTH")
    fun getAndDrop()
}