package com.delivery.sopo.data.resource.user.remote

import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword

interface UserRemoteDataSource
{
    suspend fun issueToken(userName: String, password: String): OAuthToken
    suspend fun refreshToken(userName: String, refreshToken: String): OAuthToken
    suspend fun fetchUserInfo(): UserDetail
    suspend fun updateNickname(nickname: String)
    suspend fun deleteUser(reason: String)
    suspend fun requestAuthCodeEmail(email: String): String
    suspend fun requestVerifyAuthToken(authCode: ResetAuthCode)
    suspend fun updatePassword(resetPassword: ResetPassword)
    suspend fun updateFCMToken(fcmToken: String)
}