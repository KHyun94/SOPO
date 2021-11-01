package com.delivery.sopo.networks.api

import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface JoinAPI
{
    // 자체 회원가입
    @POST("api/v1/sopo-user/join/sopo")
    @Headers("Accept: application/json")
    suspend fun requestJoinBySelf(
            @Body joinInfoDTO: JoinInfoDTO
    ): Response<Unit>

    // 카카오 회원가입
    @POST("api/v1/sopo-user/join/kakao")
    @Headers("Accept: application/json")
    suspend fun requestJoinByKakao(
            @Body joinInfoDTO: JoinInfoDTO
    ):Response<Unit>
}