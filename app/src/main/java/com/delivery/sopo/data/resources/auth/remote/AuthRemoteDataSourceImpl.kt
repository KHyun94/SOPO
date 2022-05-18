package com.delivery.sopo.data.resources.auth.remote

import com.delivery.sopo.data.networks.NetworkManager
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.services.network_handler.BaseService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthRemoteDataSourceImpl(private val dispatcher: CoroutineDispatcher): AuthRemoteDataSource, BaseService()
{
    val gson: Gson = Gson()
    val publicUserService: UserService by lazy { NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, UserService::class.java) }

    override suspend fun issueToken(userName: String, password: String): OAuthToken = withContext(dispatcher) {
        val result = apiCall { publicUserService.issueToken(grantType = "password", userName = userName, password = password) }

        val type = object: TypeToken<OAuthToken>() {}.type
        val reader = gson.toJson(result.data)
        val tokenInfo = gson.fromJson<OAuthToken>(reader, type)

        return@withContext tokenInfo
    }

    override suspend fun refreshToken(userName: String, refreshToken: String): OAuthToken  = withContext(dispatcher) {
        val publicUserService: UserService = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, UserService::class.java)
        val result = apiCall { publicUserService.refreshToken(grantType = "refresh_token", userName = userName, refreshToken = refreshToken) }

        val type = object: TypeToken<OAuthToken>() {}.type
        val reader = gson.toJson(result.data)
        val tokenInfo = gson.fromJson<OAuthToken>(reader, type)

        return@withContext tokenInfo
    }
}