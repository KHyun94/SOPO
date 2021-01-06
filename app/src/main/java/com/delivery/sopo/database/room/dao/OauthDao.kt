package com.delivery.sopo.database.room.dao

import androidx.room.*
import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.database.room.entity.WorkEntity

@Dao
interface OauthDao
{
    @Query("SELECT * FROM OAUTH WHERE email = :email")
    fun get(email : String) : OauthEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(oauth : OauthEntity)

    @Update
    fun update(oauth : OauthEntity)

    @Delete
    fun delete(oauth : OauthEntity)
}