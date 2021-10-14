package com.delivery.sopo.networks.repository

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.JoinRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.exceptions.APIBetaException
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.JoinAPI
import com.delivery.sopo.networks.call.JoinCall
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.services.network_handler.BaseServiceBeta
import com.delivery.sopo.services.network_handler.NetworkResponseBeta
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class JoinRepositoryImpl: JoinRepository, BaseServiceBeta(), KoinComponent
{
    private val joinAPI: JoinAPI = NetworkManager.retro().create(JoinAPI::class.java)

    override suspend fun requestJoinBySelf(joinInfoDTO: JoinInfoDTO) = withContext(Dispatchers.IO) {
        when(val result = apiCall(call = { joinAPI.requestJoinBySelf(joinInfoDTO) }))
        {
            is NetworkResponseBeta.Error ->
            {
                throw APIBetaException(result.statusCode, result.errorResponse)
            }
            else ->
            {
                SopoLog.d("자체 회원가입 성공 [email:${joinInfoDTO.email}]")
            }
        }
    }

    override suspend fun requestJoinByKakao(joinInfoDTO: JoinInfoDTO) =
        withContext(Dispatchers.IO) {
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


    /*    override suspend fun requestJoinBySelf(joinInfoDTO: JoinInfoDTO) = withContext(Dispatchers.IO){
            SopoLog.d("requestJoinBySelf(...) 호출")
            JoinCall.requestJoinBySelf(joinInfoDTO = joinInfoDTO)
        }

        override suspend fun requestJoinByKakao(joinInfoDTO: JoinInfoDTO)
        {
            SopoLog.d("requestJoinByKakao(...) 호출")
            JoinCall.requestJoinByKakao(joinInfoDTO = joinInfoDTO)
        }*/
}
