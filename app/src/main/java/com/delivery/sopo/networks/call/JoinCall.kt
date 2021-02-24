package com.delivery.sopo.networks.call

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.JoinAPI
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult

object JoinCall : BaseService()
{
    var joinAPI : JoinAPI

    init
    {
        NetworkManager.setLogin(null, null)
        joinAPI = NetworkManager.retro.create(JoinAPI::class.java)
    }

    suspend fun requestJoinBySelf(email : String, password : String, deviceInfo : String, firebaseUid : String) : NetworkResult<APIResult<String>>
    {
        val request = joinAPI.requestJoinBySelf(email = email, password = password, deviceInfo = deviceInfo, firebaseUid = firebaseUid)
        return apiCall(call = {request})
    }

    suspend fun requestJoinByKakao(email : String, password : String, deviceInfo : String, kakaoUid : String, firebaseUid : String) : NetworkResult<APIResult<String>>
    {
        val request = joinAPI.requestJoinByKakao(email = email, password = password, deviceInfo = deviceInfo, kakaoUid = kakaoUid, firebaseUid = firebaseUid)
        return apiCall(call = {request})
    }

    suspend fun requestDuplicatedEmail(email: String): NetworkResult<APIResult<Boolean>>
    {
        NetworkManager.setLogin(BuildConfig.PUBLIC_API_ACCOUNT_ID, BuildConfig.PUBLIC_API_ACCOUNT_PASSWORD)
        val request = joinAPI.requestDuplicateEmail(email = email)
        return apiCall(call = {request})
    }
}