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

    // TODO 자체 회원 가입 API에 닉네임이 필요없음
    suspend fun requestJoinBySelf(email : String, password : String, deviceInfo : String, nickname: String) : NetworkResult<APIResult<Unit>>
    {
        return apiCall(call = { joinAPI.requestJoinBySelf(email = email, password = password, deviceInfo = deviceInfo, nickname = nickname) })
    }

    suspend fun requestJoinByKakao(email : String, password : String, deviceInfo : String, kakaoUid : String, nickname: String) : NetworkResult<APIResult<Unit>>
    {
        return apiCall(call = { joinAPI.requestJoinByKakao(email = email, password = password, deviceInfo = deviceInfo, kakaoUid = kakaoUid,  nickname = nickname) })
    }

    suspend fun requestDuplicatedEmail(email: String): NetworkResult<APIResult<Boolean>>
    {
        NetworkManager.setLogin(BuildConfig.PUBLIC_API_ACCOUNT_ID, BuildConfig.PUBLIC_API_ACCOUNT_PASSWORD)
        return apiCall(call = { joinAPI.requestDuplicateEmail(email = email) })
    }
}