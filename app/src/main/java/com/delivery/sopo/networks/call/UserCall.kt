package com.delivery.sopo.networks.call

import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.models.PasswordResetDTO
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import org.koin.core.KoinComponent

/**
 * TODO Repository로 이전 필수
 */
object UserCall: BaseService(), KoinComponent
{
    private lateinit var userAPI: UserAPI

    suspend fun requestSignOut(reason: String): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
        return apiCall(call = { userAPI.requestSignOut(reason) })
    }

    suspend fun requestResetPassword(passwordResetDTO: PasswordResetDTO): NetworkResult<APIResult<String?>>
    {
        userAPI = NetworkManager.setLoginMethod(NetworkEnum.EMPTY_LOGIN, UserAPI::class.java)
        return apiCall(call = { userAPI.requestResetPassword(passwordResetDTO = passwordResetDTO) })
    }
}