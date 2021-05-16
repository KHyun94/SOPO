package com.delivery.sopo.data.repository.local.o_auth

import androidx.room.Transaction
import com.delivery.sopo.data.repository.database.room.AppDatabase

class OAuthLocalRepository(private val appDatabase: AppDatabase) : OAuthLocalDataSource
{
    override fun get(userId: String): OAuthEntity? = appDatabase.oauthDao().get(userId = userId)
    override fun insert(OAuth: OAuthEntity) = appDatabase.oauthDao().insert(OAuth)
    override fun update(OAuth: OAuthEntity) =  appDatabase.oauthDao().update(OAuth)
    override fun delete(OAuth: OAuthEntity) =  appDatabase.oauthDao().delete(OAuth)
}