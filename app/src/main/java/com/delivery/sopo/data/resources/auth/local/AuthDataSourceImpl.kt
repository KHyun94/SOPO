package com.delivery.sopo.data.resources.auth.local

import com.delivery.sopo.data.database.room.dao.AuthTokenDao
import com.delivery.sopo.data.models.AuthToken
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.models.api.Error
import com.delivery.sopo.models.mapper.AuthTokenMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthDataSourceImpl(private val authTokenDao: AuthTokenDao, private val dispatcher: CoroutineDispatcher): AuthDataSource
{
    override suspend fun get(): AuthToken.Info = withContext(dispatcher) {
        val entity = authTokenDao.get() ?: throw SOPOApiException(404, Error.makeError(errorCode = 102))
        return@withContext entity.run(AuthTokenMapper::entityToObject)
    }

    override suspend fun insert(token: AuthToken.Info) = withContext(dispatcher) {
        val entity = AuthTokenMapper.objectToEntity(dto = token)
        authTokenDao.insert(entity)
    }

    override suspend fun update(token: AuthToken.Info) = withContext(dispatcher) {
        val entity = AuthTokenMapper.objectToEntity(dto = token)
        authTokenDao.update(entity)
    }

    override suspend fun delete(token: AuthToken.Info) = withContext(dispatcher) {
        val entity = AuthTokenMapper.objectToEntity(dto = token)
        authTokenDao.delete(entity)
    }
}