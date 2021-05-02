package com.delivery.sopo.networks.call

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.JoinAPI
import com.delivery.sopo.networks.dto.joins.JoinInfoByKakaoDTO
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
    suspend fun requestJoinBySelf(joinInfoByKakaoDTO: JoinInfoByKakaoDTO) : NetworkResult<APIResult<Unit>>
    {
        return apiCall(call = { joinAPI.requestJoinBySelf(joinInfoByKakaoDTO) })
    }

    suspend fun requestJoinByKakao(joinInfoByKakaoDTO: JoinInfoByKakaoDTO) : NetworkResult<APIResult<Unit>>
    {
        return apiCall(call = { joinAPI.requestJoinByKakao(joinInfoByKakaoDTO) })
    }

    suspend fun requestDuplicatedEmail(email: String): NetworkResult<APIResult<Boolean>>
    {
        NetworkManager.setLogin(BuildConfig.PUBLIC_API_ACCOUNT_ID, BuildConfig.PUBLIC_API_ACCOUNT_PASSWORD)
        return apiCall(call = { joinAPI.requestDuplicateEmail(email = email) })
    }
}