package com.delivery.sopo.networks.call

import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.OAuthAPI
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult

object OAuthCall : BaseService()
{
    private val oAuthAPI = NetworkManager.retro("sopo-aos", "sopoAndroid!!@@").create(OAuthAPI::class.java)

//    init
//    {
//        NetworkManager.setLogin("sopo-aos", "sopoAndroid!!@@")
//    }

    suspend fun checkOAuthToken(accessToken : String) : NetworkResult<Any>
    {
        val request = oAuthAPI.checkOAuthToken(accessToken = accessToken)
        return apiCall(call = { request })
    }
}