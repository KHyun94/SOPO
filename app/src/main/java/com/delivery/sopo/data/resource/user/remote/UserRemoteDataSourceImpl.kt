package com.delivery.sopo.data.resource.user.remote

import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.extensions.wrapBodyAliasToMap
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.services.network_handler.BaseService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserRemoteDataSourceImpl(private val userService: UserService):UserRemoteDataSource, BaseService()
{
    val gson: Gson = Gson()

    override suspend fun issueToken(userName: String, password: String): OAuthToken
    {
        val result = apiCall { userService.issueToken(grantType = "password", userName = userName, password = password) }

        val type = object: TypeToken<OAuthToken>() {}.type
        val reader = gson.toJson(result.data)
        val tokenInfo = gson.fromJson<OAuthToken>(reader, type)

        return tokenInfo
    }

    override suspend fun refreshToken(userName: String, refreshToken: String): OAuthToken
    {
        val result = apiCall { userService.refreshToken(grantType = "refresh_token", userName = userName, refreshToken = refreshToken) }

        val type = object: TypeToken<OAuthToken>() {}.type
        val reader = gson.toJson(result.data)
        val tokenInfo = gson.fromJson<OAuthToken>(reader, type)

        return tokenInfo
    }

    override suspend fun fetchUserInfo(): UserDetail
    {
        val result = apiCall { userService.fetchUserInfo() }
        return result.data?.data ?: throw SOPOApiException(404, ErrorResponse(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))
    }

    override suspend fun updateNickname(nickname: String)
    {
        val wrapParameter = nickname.wrapBodyAliasToMap("nickname")
        apiCall { userService.updateNickname(nickname = wrapParameter) }
    }

    override suspend fun deleteUser(reason: String)
    {
        val wrapParameter = reason.wrapBodyAliasToMap("reason")
        userService.deleteUser(reason = wrapParameter)
    }

    override suspend fun requestAuthCodeEmail(email: String): String
    {
        val result = apiCall { userService.requestAuthCodeEmail(email = email) }
        return result.data?.data?: throw SOPOApiException(404, ErrorResponse(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))
    }

    override suspend fun requestVerifyAuthToken(authCode: String)
    {
        val result = apiCall { userService.requestVerifyAuthToken(authCode = authCode) }
    }

    override suspend fun updatePassword()
    {
        TODO("Not yet implemented")
    }

    override suspend fun updateFCMToken()
    {
        TODO("Not yet implemented")
    }
}