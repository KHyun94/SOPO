package com.delivery.sopo.data.resources.auth.remote

import com.delivery.sopo.models.dto.OAuthToken

interface AuthRemoteDataSource
{
    suspend fun issueToken(userName: String, password: String): OAuthToken
    suspend fun refreshToken(userName: String, refreshToken: String): OAuthToken
}