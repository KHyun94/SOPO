package com.delivery.sopo.data.resources.user.remote

import com.delivery.sopo.data.networks.dto.joins.JoinInfo
import com.delivery.sopo.services.network_handler.NetworkResponse

interface SignUpRemoteDataSource
{
    suspend fun requestJoinBySelf(joinInfo: JoinInfo): NetworkResponse<Unit>
    suspend fun requestJoinByKakao(joinInfo: JoinInfo): NetworkResponse<Unit>
}