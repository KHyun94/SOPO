package com.delivery.sopo.database.room.dao

import androidx.room.*
import com.delivery.sopo.database.room.entity.OauthEntity

@Dao
interface OAuthDao
{
    @Query("SELECT * FROM OAUTH WHERE email = :email")
    fun get(email : String) : OauthEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(Oauth : OauthEntity)
    @Update
    fun update(Oauth : OauthEntity)
    @Delete
    fun delete(Oauth : OauthEntity)
}