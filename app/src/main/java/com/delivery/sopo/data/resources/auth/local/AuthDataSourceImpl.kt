package com.delivery.sopo.data.resources.auth.local

import com.delivery.sopo.data.database.datastore.DataStoreKey.ACCESS_TOKEN
import com.delivery.sopo.data.database.datastore.DataStoreKey.EXPIRE_AT
import com.delivery.sopo.data.database.datastore.DataStoreKey.GRANT_TYPE
import com.delivery.sopo.data.database.datastore.DataStoreKey.REFRESH_TOKEN
import com.delivery.sopo.data.database.datastore.DataStoreKey.USER_TOKEN
import com.delivery.sopo.data.database.datastore.DataStoreManager
import com.delivery.sopo.data.models.AuthToken
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.models.api.Error
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthDataSourceImpl(private val dataStoreManager: DataStoreManager, private val dispatcher: CoroutineDispatcher): AuthDataSource
{
   override suspend fun insert(token: AuthToken.Info) = withContext(dispatcher){
        token.run {
            dataStoreManager.storeValue(USER_TOKEN, userToken)
            dataStoreManager.storeValue(ACCESS_TOKEN, accessToken)
            dataStoreManager.storeValue(REFRESH_TOKEN, refreshToken)
            dataStoreManager.storeValue(GRANT_TYPE, grantType)
            dataStoreManager.storeValue(EXPIRE_AT, expireAt)
        }
    }

    override suspend fun getAccessToken(): String = withContext(dispatcher)
    {
        return@withContext dataStoreManager.readValue(ACCESS_TOKEN) ?: throw SOPOApiException(404, Error.makeError(errorCode = 102))
    }

    override suspend fun getRefreshToken(): String = withContext(dispatcher)
    {
        return@withContext dataStoreManager.readValue(REFRESH_TOKEN)?: throw SOPOApiException(404, Error.makeError(errorCode = 102))
    }

    override suspend fun getExpireAt(): String = withContext(dispatcher) {
        return@withContext  dataStoreManager.readValue(EXPIRE_AT)?: throw SOPOApiException(404, Error.makeError(errorCode = 102))
    }

    override suspend fun updateAccessToken(accessToken: String) = withContext(dispatcher)
    {
        dataStoreManager.storeValue(ACCESS_TOKEN, accessToken)
    }
}