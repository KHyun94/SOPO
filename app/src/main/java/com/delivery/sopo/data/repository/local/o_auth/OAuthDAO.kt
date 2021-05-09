package com.delivery.sopo.data.repository.local.o_auth

import androidx.room.*

@Dao
interface OAuthDAO
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