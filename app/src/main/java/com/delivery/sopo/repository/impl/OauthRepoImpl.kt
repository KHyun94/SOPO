package com.delivery.sopo.repository.impl

import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.repository.interfaces.OauthRepository

class OauthRepoImpl(private val appDatabase: AppDatabase) : OauthRepository
{
    override fun get(email: String): OauthEntity? = appDatabase.oauthDao().get(email = email)
    override fun insert(Oauth: OauthEntity) = appDatabase.oauthDao().insert(Oauth)
    override fun update(Oauth: OauthEntity) =  appDatabase.oauthDao().update(Oauth)
    override fun delete(Oauth: OauthEntity) =  appDatabase.oauthDao().delete(Oauth)
}