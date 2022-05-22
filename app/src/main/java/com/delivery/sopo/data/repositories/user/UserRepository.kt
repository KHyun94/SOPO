package com.delivery.sopo.data.repositories.user

import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword

interface UserRepository
{
    suspend fun login()
    suspend fun login(username: String, password: String)
    suspend fun refreshToken(): String
    suspend fun fetchUserInfo()
    suspend fun checkExpiredTokenWithInWeek():Boolean
    suspend fun updateNickname(nickname: String)
    suspend fun updateFCMToken(fcmToken: String)
    suspend fun requestAuthCodeEmail(email: String)
    suspend fun requestVerifyAuthToken(authCode: ResetAuthCode)
    suspend fun updatePassword(resetPassword: ResetPassword)
    suspend fun deleteUser(reason: String)
}