package com.delivery.sopo.data.repositories.local.o_auth

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.entity.OAuthEntity
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.models.mapper.OAuthMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OAuthLocalRepository(private val appDatabase: AppDatabase): OAuthLocalDataSource
{

    override suspend fun get(userId: String) = withContext(Dispatchers.Default) {
        appDatabase.oauthDao().get(userId = userId) ?: throw NullPointerException("OAuth Token 정보가 없습니다.")
    }.run(OAuthMapper::entityToObject)


    override fun insert(token: OAuthToken)
    {
        val entity = OAuthMapper.objectToEntity(oAuth = token)
        appDatabase.oauthDao().insert(entity)
    }

    override fun update(OAuth: OAuthEntity) = appDatabase.oauthDao().update(OAuth)
    override fun delete(OAuth: OAuthEntity) = appDatabase.oauthDao().delete(OAuth)
}