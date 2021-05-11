package com.delivery.sopo.networks.call

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.JoinAPI
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult

object JoinCall : BaseService()
{
    var joinAPI : JoinAPI

    init
    {
//        NetworkManager.setLogin(null, null)
//        joinAPI = NetworkManager.retro.create(JoinAPI::class.java)
        joinAPI = NetworkManager.retro().create(JoinAPI::class.java)
    }

    // TODO 자체 회원 가입 API에 닉네임이 필요없음
    suspend fun requestJoinBySelf(joinInfoDTO: JoinInfoDTO) = apiCall(call = { joinAPI.requestJoinBySelf(joinInfoDTO) })
    suspend fun requestJoinByKakao(joinInfoDTO: JoinInfoDTO) = apiCall(call = { joinAPI.requestJoinByKakao(joinInfoDTO) })
}