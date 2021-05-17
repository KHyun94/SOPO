package com.delivery.sopo.networks.call

import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import org.koin.core.KoinComponent

object UserCall : BaseService(), KoinComponent
{
    private lateinit var userAPI : UserAPI

    suspend fun getUserInfoWithToken() : NetworkResult<APIResult<UserDetail?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        val result = userAPI.getUserInfoWithToken()
        return apiCall(call = {result})
    }

    suspend fun updateFCMToken(fcmToken: String) : NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        return apiCall (call = {userAPI.updateFCMToken(fcmToken = fcmToken)} )
    }

    // TODO error 닉네임 설정
    suspend fun updateNickname(nickname: String): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        return apiCall (call = {userAPI.updateUserNickname(nickname = nickname)} )
    }

    suspend fun requestSignOut(reason: String): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        return apiCall(call = { userAPI.requestSignOut(reason) })
    }

    suspend fun requestEmailForAuth(email: String): NetworkResult<APIResult<EmailAuthDTO?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.EMPTY_LOGIN, UserAPI::class.java)
        return apiCall(call = { userAPI.requestEmailForAuth(email = email) })
    }
}