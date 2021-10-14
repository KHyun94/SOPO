package com.delivery.sopo.data.repository

import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import kotlinx.coroutines.Job

interface JoinRepository
{
    suspend fun requestJoinBySelf(joinInfoDTO: JoinInfoDTO)
    suspend fun requestJoinByKakao(joinInfoDTO: JoinInfoDTO)
}