package com.delivery.sopo.networks.repository

import com.delivery.sopo.data.repository.JoinRepository
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.JoinAPI
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.services.network_handler.BaseServiceBeta
import com.delivery.sopo.services.network_handler.NetworkResponse
import kotlinx.coroutines.*
import org.koin.core.KoinComponent

class JoinRepositoryImpl: JoinRepository, BaseServiceBeta(), KoinComponent
{
    private val joinAPI: JoinAPI = NetworkManager.retro().create(JoinAPI::class.java)

    override suspend fun requestJoinBySelf(joinInfoDTO: JoinInfoDTO): NetworkResponse<Unit> = withContext(Dispatchers.IO) {
        return@withContext apiCall(call = { joinAPI.requestJoinBySelf(joinInfoDTO) })
    }

    override suspend fun requestJoinByKakao(joinInfoDTO: JoinInfoDTO): NetworkResponse<Unit> = withContext(Dispatchers.IO) {
            return@withContext apiCall(call = { joinAPI.requestJoinByKakao(joinInfoDTO) })
    }
}
