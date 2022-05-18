package com.delivery.sopo.data.resources.user.remote

import com.delivery.sopo.data.networks.NetworkManager
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.extensions.wrapBodyAliasToMap
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.services.network_handler.BaseService
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher

class UserRemoteDataSourceImpl(private val dispatcher: CoroutineDispatcher): UserRemoteDataSource, BaseService()
{
    val gson: Gson = Gson()

    val publicUserService: UserService by lazy { NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, UserService::class.java) }

    override suspend fun requestAuthCodeEmail(email: String): String
    {
        val result = apiCall { publicUserService.requestAuthCodeEmail(email = email) }
        return result.data?.data?: throw SOPOApiException(404, ErrorResponse(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))
    }

    override suspend fun requestVerifyAuthToken(authCode: ResetAuthCode)
    {
        apiCall { publicUserService.requestVerifyAuthToken(authCode = authCode) }
    }

    override suspend fun updatePassword(resetPassword: ResetPassword)
    {
        apiCall { publicUserService.updatePassword(resetPassword = resetPassword) }
    }

    override suspend fun fetchUserInfo(): UserDetail
    {
        val privateUserService: UserService = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserService::class.java)
        val result = apiCall { privateUserService.fetchUserInfo() }
        return result.data?.data ?: throw SOPOApiException(404, ErrorResponse(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))
    }

    override suspend fun updateNickname(nickname: String)
    {
        val privateUserService: UserService = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserService::class.java)
        val wrapParameter = nickname.wrapBodyAliasToMap("nickname")
        apiCall { privateUserService.updateNickname(nickname = wrapParameter) }
    }

    override suspend fun deleteUser(reason: String)
    {
        val privateUserService: UserService = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserService::class.java)
        val wrapParameter = reason.wrapBodyAliasToMap("reason")
        apiCall { privateUserService.deleteUser(reason = wrapParameter) }
    }

    override suspend fun updateFCMToken(fcmToken: String)
    {
        val privateUserService: UserService = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserService::class.java)
        val wrapParameter = fcmToken.wrapBodyAliasToMap("fcmToken")
        apiCall { privateUserService.updateFCMToken(fcmToken = wrapParameter) }
    }
}