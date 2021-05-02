package com.delivery.sopo.networks.api

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.dto.joins.JoinInfoByKakaoDTO
import retrofit2.Response
import retrofit2.http.*

interface JoinAPI
{
    // 자체 회원가입
    @POST("api/v1/sopo-api/join/sopo")
    @Headers("Accept: application/json")
    suspend fun requestJoinBySelf(
        @Body joinInfoByKakaoDTO: JoinInfoByKakaoDTO
        ): Response<APIResult<Unit>>

    // 카카오 회원가입
    @POST("api/v1/sopo-api/join/kakao")
    @Headers("Accept: application/json")
    suspend fun requestJoinByKakao(
        @Body joinInfoByKakaoDTO: JoinInfoByKakaoDTO
    ):Response<APIResult<Unit>>

    // 회원가입 이메일 중복 체크
    @GET("api/v1/sopo-api/join/email/exist/{email}")
    @Headers("Accept: application/json")
    suspend fun requestDuplicateEmail(
        @Path("email") email: String
    ): Response<APIResult<Boolean>>
}