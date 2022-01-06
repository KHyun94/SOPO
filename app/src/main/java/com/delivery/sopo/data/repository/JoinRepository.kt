package com.delivery.sopo.data.repository

import com.delivery.sopo.networks.dto.joins.JoinInfo
import com.delivery.sopo.services.network_handler.NetworkResponse

interface JoinRepository
{
    suspend fun requestJoinBySelf(joinInfo: JoinInfo): NetworkResponse<Unit>
    suspend fun requestJoinByKakao(joinInfo: JoinInfo): NetworkResponse<Unit>
}