package com.delivery.sopo.repository.interfaces

import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.OauthEntity

interface OauthRepository
{
    fun get(email : String) : OauthEntity?
    fun insert(oauth : OauthEntity)
    fun update(oauth : OauthEntity)
    fun delete(oauth : OauthEntity)
}