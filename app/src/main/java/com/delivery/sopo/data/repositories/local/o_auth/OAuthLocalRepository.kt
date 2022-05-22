package com.delivery.sopo.data.repositories.local.o_auth

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.entity.AuthTokenEntity
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.models.mapper.AuthTokenMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OAuthLocalRepository(private val appDatabase: AppDatabase): OAuthLocalDataSource
{

    override suspend fun get(userId: String) = withContext(Dispatchers.Default) {
        appDatabase.oauthDao().get(userId = userId) ?: throw NullPointerException("OAuth Token 정보가 없습니다.")
    }.run(AuthTokenMapper::entityToObject)


    override fun insert(token: OAuthToken)
    {
        val entity = AuthTokenMapper.objectToEntity(oAuth = token)
        appDatabase.oauthDao().insert(entity)
    }

    override fun update(authToken: AuthTokenEntity) = appDatabase.oauthDao().update(authToken)
    override fun delete(authToken: AuthTokenEntity) = appDatabase.oauthDao().delete(authToken)
}