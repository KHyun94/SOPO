package com.delivery.sopo.data.repository.local.o_auth

import androidx.room.Transaction
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.models.mapper.OAuthMapper

class OAuthLocalRepository(private val appDatabase: AppDatabase) : OAuthLocalDataSource
{
    override fun get(userId: String): OAuthEntity? = appDatabase.oauthDao().get(userId = userId)
    override fun insert(dto: OAuthDTO)
    {
        val entity = OAuthMapper.objectToEntity(oAuth = dto)
        appDatabase.oauthDao().insert(entity)
    }
    override fun update(OAuth: OAuthEntity) =  appDatabase.oauthDao().update(OAuth)
    override fun delete(OAuth: OAuthEntity) =  appDatabase.oauthDao().delete(OAuth)
}