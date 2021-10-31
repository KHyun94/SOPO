package com.delivery.sopo.data.repository

import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.services.network_handler.NetworkResponse
import kotlinx.coroutines.Job

interface JoinRepository
{
    suspend fun requestJoinBySelf(joinInfoDTO: JoinInfoDTO): NetworkResponse<Unit>
    suspend fun requestJoinByKakao(joinInfoDTO: JoinInfoDTO): NetworkResponse<Unit>
}