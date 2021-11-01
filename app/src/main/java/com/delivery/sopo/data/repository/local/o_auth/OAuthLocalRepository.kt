package com.delivery.sopo.data.repository.local.o_auth

import androidx.room.Transaction
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.networks.call.ParcelCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OAuthLocalRepository(private val appDatabase: AppDatabase): OAuthLocalDataSource
{

    override suspend fun get(userId: String) = withContext(Dispatchers.Default) {
        appDatabase.oauthDao().get(userId = userId) ?: throw NullPointerException("OAuth Token 정보가 없습니다.")
    }.run(OAuthMapper::entityToObject)


    override fun insert(dto: OAuthDTO)
    {
        val entity = OAuthMapper.objectToEntity(oAuth = dto)
        appDatabase.oauthDao().insert(entity)
    }

    override fun update(OAuth: OAuthEntity) = appDatabase.oauthDao().update(OAuth)
    override fun delete(OAuth: OAuthEntity) = appDatabase.oauthDao().delete(OAuth)
}