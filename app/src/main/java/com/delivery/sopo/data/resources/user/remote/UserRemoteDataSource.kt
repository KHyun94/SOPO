package com.delivery.sopo.data.resources.user.remote

import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword

interface UserRemoteDataSource
{
    suspend fun fetchUserInfo(): UserDetail
    suspend fun updateNickname(nickname: String)
    suspend fun deleteUser(reason: String)
    suspend fun requestAuthCodeEmail(email: String): String
    suspend fun requestVerifyAuthToken(authCode: ResetAuthCode)
    suspend fun updatePassword(resetPassword: ResetPassword)
    suspend fun updateFCMToken(fcmToken: String)
}