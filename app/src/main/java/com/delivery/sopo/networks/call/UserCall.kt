package com.delivery.sopo.networks.call

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.models.PasswordResetDTO
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.OAuthAPI
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import org.koin.core.KoinComponent
import org.koin.core.inject

object UserCall: BaseService(), KoinComponent
{
    private val userRepo: UserLocalRepository by inject()
    private val oAuthRepo: OAuthLocalRepository by inject()
    private lateinit var userAPI: UserAPI

    suspend fun getUserDetailInfo(): NetworkResult<APIResult<UserDetail>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        val result = userAPI.getUserDetailInfo()
        return apiCall(call = { result })
    }

    suspend fun requestRefreshTokenInOAuth(): NetworkResult<Any>
    {
        val oAuthEntity = oAuthRepo.get(userId = userRepo.getUserId()) ?:  throw Exception("o auth data is null")
        val oAuthDTO = OAuthMapper.entityToObject(oAuthEntity)


        val oAuthAPI = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, OAuthAPI::class.java)
        val result = oAuthAPI.requestRefreshTokenInOAuth(grantType = "refresh_token",
                                            email = userRepo.getUserId(),
                                            refreshToken = oAuthDTO.refreshToken,
                                            deviceInfo = SOPOApp.deviceInfo)
        return apiCall(call = {result})
    }

    suspend fun updateFCMToken(fcmToken: Map<String, String>): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        return apiCall(call = { userAPI.updateFCMToken(fcmToken = fcmToken) })
    }

    suspend fun updateNickname(nickname: String): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        return apiCall(call = {
            userAPI.updateUserNickname(nickname = mapOf<String, String>(Pair("nickname", nickname)))
        })
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

    suspend fun requestResetPassword(passwordResetDTO: PasswordResetDTO): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.EMPTY_LOGIN, UserAPI::class.java)
        return apiCall(call = { userAPI.requestResetPassword(passwordResetDTO = passwordResetDTO) })
    }
}