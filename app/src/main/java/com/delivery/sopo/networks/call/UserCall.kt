package com.delivery.sopo.networks.call

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

object UserCall : BaseService()
{
    val TAG = this.javaClass.simpleName
    private lateinit var userAPI : UserAPI

    suspend fun getUserInfoWithToken() : NetworkResult<APIResult<UserDetail?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        val result = userAPI.getUserInfoWithToken()
        return apiCall(call = {result})
    }

    suspend fun patchUser(email: String, jwtToken: String, jsonPatch: JsonPatchDto?): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.PUBLIC_LOGIN, UserAPI::class.java)
        val patchUser = userAPI.test(email = email, jwtToken = jwtToken, jsonPatch = jsonPatch)
        return apiCall(call = { patchUser })
    }

    suspend fun requestCustomToken(
        email: String,
        deviceInfo: String,
        joinType: String,
        userId: String
    ): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.PUBLIC_LOGIN, UserAPI::class.java)
        val requestCustomToken = userAPI.requestCustomToken(
            email = email,
            deviceInfo = deviceInfo,
            joinType = joinType,
            userId = userId
        )
        return apiCall(call = { requestCustomToken })
    }

    suspend fun updateFCMToken(fcmToken: String) : NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        return apiCall (call = {userAPI.updateFCMToken(fcmToken = fcmToken)} )
    }

    suspend fun updateNickname(nickname: String): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        return apiCall (call = {userAPI.updateUserNickname(nickname = nickname)} )
    }
}