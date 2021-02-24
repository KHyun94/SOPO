package com.delivery.sopo.networks.api

import com.delivery.sopo.models.api.APIResult
import retrofit2.Response
import retrofit2.http.*

interface JoinAPI
{
    // 자체 회원가입
    @FormUrlEncoded
    @POST("api/v1/sopo-api/join/sopo")
    @Headers("Accept: application/json")
    suspend fun requestJoinBySelf(
        // 유저 이메일
        @Field("email") email: String,
        // 비밀번호
        @Field("password") password: String,
        // 디바이스 정보
        @Field("deviceInfo") deviceInfo: String,
        // Firebase uid
        @Field("uid") firebaseUid: String
    ): Response<APIResult<String>>

    // 카카오 회원가입
    @FormUrlEncoded
    @POST("api/v1/sopo-api/join/kakao")
    @Headers("Accept: application/json")
    suspend fun requestJoinByKakao(
        // 유저 이메일
        @Field("email") email: String,
        // 비밀번호
        @Field("password") password: String,
        // 디바이스 정보
        @Field("deviceInfo") deviceInfo: String,
        // Firebase uid
        @Field("kakaoUserId") kakaoUid: String,
        // Firebase uid
        @Field("uid") firebaseUid: String
    ):Response<APIResult<String>>

    // 회원가입 이메일 중복 체크
    @GET("api/v1/sopo-api/validation/email/exist/{email}")
    @Headers("Accept: application/json")
    suspend fun requestDuplicateEmail(
        @Path("email") email: String
    ): Response<APIResult<Boolean>>
}