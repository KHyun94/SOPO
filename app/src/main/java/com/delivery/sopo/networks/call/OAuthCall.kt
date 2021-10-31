package com.delivery.sopo.networks.call

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.OAuthAPI
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import org.koin.core.KoinComponent
import org.koin.core.inject

object OAuthCall: BaseService(), KoinComponent
{
    private val userRepo: UserLocalRepository by inject()
    private val oAuthRepo: OAuthLocalRepository by inject()

    /*suspend fun requestOauth(email: String, password: String, deviceInfo: String): NetworkResult<Any>
    {
        val requestOauth = NetworkManager.retro(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_PASSWORD)
            .create(OAuthAPI::class.java)
            .requestQAuthToken(grantType = "password", email = email, password = password.toMD5(),
                               deviceInfo = deviceInfo)

        return apiCall(call = { requestOauth })
    }
*/
    suspend fun requestRefreshTokenInOAuth(): NetworkResult<Any>
    {
        val oAuthEntity =
            oAuthRepo.get(userId = userRepo.getUserId()) ?: throw Exception("o auth data is null")
        val oAuthDTO = OAuthMapper.entityToObject(oAuthEntity)


        val oAuthAPI =
            NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, OAuthAPI::class.java)
        val result = oAuthAPI.requestRefreshTokenInOAuth(grantType = "refresh_token",
                                                         email = userRepo.getUserId(),
                                                         refreshToken = oAuthDTO.refreshToken)
        return apiCall(call = { result })
    }
}