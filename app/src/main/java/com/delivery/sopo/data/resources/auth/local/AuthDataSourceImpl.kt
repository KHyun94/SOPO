package com.delivery.sopo.data.resources.auth.local

import com.delivery.sopo.data.database.room.dao.OAuthDao
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.models.mapper.OAuthMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthDataSourceImpl(private val oAuthDao: OAuthDao, private val dispatcher: CoroutineDispatcher): AuthDataSource
{
    override suspend fun get(userName: String): OAuthToken = withContext(dispatcher) {
        oAuthDao.get(userId = userName)
    }?.run(OAuthMapper::entityToObject)?: throw SOPOApiException(404, ErrorResponse(810, ErrorType.OAUTH2, "조회한 데이터가 존재하지 않습니다.", ""))

    override suspend fun insert(token: OAuthToken) = withContext(dispatcher) {
        val entity = OAuthMapper.objectToEntity(oAuth = token)
        oAuthDao.insert(entity)
    }

    override suspend fun update(token: OAuthToken) = withContext(dispatcher) {
        val entity = OAuthMapper.objectToEntity(oAuth = token)
        oAuthDao.update(entity)
    }

    override suspend fun delete(token: OAuthToken) = withContext(dispatcher) {
        val entity = OAuthMapper.objectToEntity(oAuth = token)
        oAuthDao.delete(entity)
    }
}