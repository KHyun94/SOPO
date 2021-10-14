package com.delivery.sopo.networks.api

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import retrofit2.Response
import retrofit2.http.*

interface JoinAPI
{
    /*// 자체 회원가입
    @POST("api/v1/sopo-api/join/sopo")
    @Headers("Accept: application/json")
    suspend fun requestJoinBySelf(
        @Body joinInfoDTO: JoinInfoDTO
        ): Response<APIResult<Unit>>

    // 카카오 회원가입
    @POST("api/v1/sopo-api/join/kakao")
    @Headers("Accept: application/json")
    suspend fun requestJoinByKakao(
        @Body joinInfoDTO: JoinInfoDTO
    ):Response<APIResult<Unit>>*/

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