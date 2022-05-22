package com.delivery.sopo.data.resources.auth.remote

import com.delivery.sopo.data.models.AuthToken

interface AuthRemoteDataSource
{
    suspend fun issueToken(username: String, password: String): AuthToken.Info
    suspend fun refreshToken(refreshToken: String): AuthToken.Info
}