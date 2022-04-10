package com.delivery.sopo.networks.repository

import com.delivery.sopo.data.repository.JoinRepository
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.JoinAPI
import com.delivery.sopo.networks.dto.joins.JoinInfo
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResponse
import kotlinx.coroutines.*
import org.koin.core.KoinComponent

class JoinRepositoryImpl: JoinRepository, BaseService(), KoinComponent
{
    private val joinAPI: JoinAPI = NetworkManager.retro().create(JoinAPI::class.java)

    override suspend fun requestJoinBySelf(joinInfo: JoinInfo): NetworkResponse<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext apiCall { joinAPI.requestJoinBySelf(joinInfo) }
        }

    override suspend fun requestJoinByKakao(joinInfo: JoinInfo): NetworkResponse<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext apiCall { joinAPI.requestJoinByKakao(joinInfo) }
        }
}
