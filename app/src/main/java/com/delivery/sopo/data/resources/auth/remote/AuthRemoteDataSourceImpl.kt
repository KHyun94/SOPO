package com.delivery.sopo.data.resources.auth.remote

import android.content.Context
import com.delivery.sopo.data.models.AuthToken
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.presentation.services.network_handler.BaseService
import com.delivery.sopo.util.OtherUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
        private val context: Context,
        private val dispatcher: CoroutineDispatcher,
        private val userService: UserService): AuthRemoteDataSource, BaseService()
{
    val gson: Gson = Gson()

    override suspend fun issueToken(username: String, password: String): AuthToken.Info =
        withContext(dispatcher) {
            val request =
                AuthToken.Request(username = username, password = password, deviceId = OtherUtil.getDeviceID(context = context))
            val result = apiCall { userService.issueToken(request = request) }

            val type = object: TypeToken<AuthToken.Info>()
            {}.type
            val reader = gson.toJson(result.data)
            val tokenInfo = gson.fromJson<AuthToken.Info>(reader, type)

            return@withContext tokenInfo
        }

    override suspend fun refreshToken(refreshToken: String): AuthToken.Info =
        withContext(dispatcher) {
            val request =
                AuthToken.Refresh(refreshToken = refreshToken, deviceId = OtherUtil.getDeviceID(context = context))
            val result = apiCall { userService.refreshToken(request = request) }

            val type = object: TypeToken<AuthToken.Info>()
            {}.type
            val reader = gson.toJson(result.data)
            val tokenInfo = gson.fromJson<AuthToken.Info>(reader, type)

            return@withContext tokenInfo
        }
}