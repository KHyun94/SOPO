package com.delivery.sopo.data.resources.user.remote

import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.extensions.wrapBodyAliasToMap
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.Error
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.presentation.services.network_handler.BaseService
import javax.inject.Inject

class UserRemoteDataSourceImpl @Inject constructor(
        private val userPubService: UserService,
        private val userPriService: UserService,
): UserRemoteDataSource, BaseService()
{
    override suspend fun requestAuthCodeEmail(email: String): String
    {
        val result = apiCall { userPubService.requestAuthCodeEmail(email = email) }
        return result.data
            ?: throw SOPOApiException(404, Error(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))
    }

    override suspend fun requestVerifyAuthToken(authCode: ResetAuthCode)
    {
        apiCall { userPubService.requestVerifyAuthToken(authCode = authCode) }
    }

    override suspend fun updatePassword(resetPassword: ResetPassword)
    {
        apiCall { userPubService.updatePassword(resetPassword = resetPassword) }
    }

    override suspend fun fetchUserInfo(): UserDetail
    {
        val result = apiCall { userPriService.fetchUserInfo() }
        return result.data
            ?: throw SOPOApiException(404, Error(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))
    }

    override suspend fun updateNickname(nickname: String)
    {
        val wrapParameter = nickname.wrapBodyAliasToMap("nickname")
        apiCall { userPriService.updateNickname(nickname = wrapParameter) }
    }

    override suspend fun deleteUser(reason: String)
    {
        val wrapParameter = reason.wrapBodyAliasToMap("reason")
        apiCall { userPriService.deleteUser(reason = wrapParameter) }
    }

    override suspend fun updateFCMToken(fcmToken: String)
    {
        val wrapParameter = fcmToken.wrapBodyAliasToMap("fcmToken")
        apiCall { userPriService.updateFCMToken(fcmToken = wrapParameter) }
    }
}