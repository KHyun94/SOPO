package com.delivery.sopo.data.networks.serivces

import com.delivery.sopo.data.models.JoinInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SignUpService
{
    // 자체 회원가입
    @POST("api/v1/sopo-user/join/sopo")
    suspend fun signUpBySelf(@Body joinInfo: JoinInfo): Response<Unit>

    // 카카오 회원가입
    @POST("api/v1/sopo-user/join/kakao")
    suspend fun signUpByKakao(@Body joinInfo: JoinInfo):Response<Unit>
}