package com.delivery.sopo.repository.impl

import androidx.room.*
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.repository.interfaces.OauthRepository

class OauthRepoImpl(private val appDatabase: AppDatabase) : OauthRepository
{
    override fun get(email: String): OauthEntity? = appDatabase.oauthDao().get(email = email)
    override fun insert(oauth: OauthEntity) = appDatabase.oauthDao().insert(oauth)
    override fun update(oauth: OauthEntity) =  appDatabase.oauthDao().update(oauth)
    override fun delete(oauth: OauthEntity) =  appDatabase.oauthDao().delete(oauth)
}