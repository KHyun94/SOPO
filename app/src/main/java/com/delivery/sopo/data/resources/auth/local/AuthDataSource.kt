package com.delivery.sopo.data.resources.auth.local

import com.delivery.sopo.data.models.AuthToken

interface AuthDataSource
{
    suspend fun insert(token: AuthToken.Info)
    suspend fun getAccessToken(): String
    suspend fun getRefreshToken(): String
    suspend fun getExpireAt(): String
    suspend fun updateAccessToken(accessToken: String)
}