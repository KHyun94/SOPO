package com.delivery.sopo.data.resource.auth.remote

import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword

interface AuthRemoteDataSource
{
    suspend fun issueToken(userName: String, password: String): OAuthToken
    suspend fun refreshToken(userName: String, refreshToken: String): OAuthToken
}