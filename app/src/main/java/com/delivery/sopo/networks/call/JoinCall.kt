package com.delivery.sopo.networks.call

import com.delivery.sopo.exceptions.APIBetaException
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.JoinAPI
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.services.network_handler.BaseServiceBeta
import com.delivery.sopo.services.network_handler.NetworkResponseBeta
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object JoinCall: BaseServiceBeta()
{
    var joinAPI: JoinAPI = NetworkManager.retro().create(JoinAPI::class.java)

    suspend fun requestJoinBySelf(joinInfoDTO: JoinInfoDTO) = withContext(Dispatchers.IO) {
        when(val result = apiCall(call = { joinAPI.requestJoinBySelf(joinInfoDTO) }))
        {
            is NetworkResponseBeta.Error ->
            {
                throw APIBetaException(result.statusCode, result.errorResponse)
            }
            else ->
            {
                withContext(Dispatchers.Default) {
                   /* userLocalRepo.setUserId(userId = joinInfoDTO.email)
                    userLocalRepo.setUserPassword(password = joinInfoDTO.password.toMD5())*/
                }
                SopoLog.d("자체 회원가입 성공 [email:${joinInfoDTO.email}]")
            }
        }
    }

    suspend fun requestJoinByKakao(joinInfoDTO: JoinInfoDTO) = withContext(Dispatchers.IO) {
        when(val result = apiCall(call = { joinAPI.requestJoinByKakao(joinInfoDTO) }))
        {
            is NetworkResponseBeta.Error ->
            {
                throw APIBetaException(result.statusCode, result.errorResponse)
            }
            else ->
            {
                SopoLog.d("카카오 간편 회원가입 성공 [email:${joinInfoDTO.email}]")
            }
        }
    }
}