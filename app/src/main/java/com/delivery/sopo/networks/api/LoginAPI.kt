package com.delivery.sopo.networks.api

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.LoginResult
import retrofit2.Call
import retrofit2.http.*

interface LoginAPI
{
    // 카카오 간편 로그인
    @FormUrlEncoded
    @POST("api/v1/sopo-api/login/kakao")
    @Headers("Accept: application/json")
    fun requestKakaoLogin(
        // 이메일
        @Field("email") email: String,
        // 디바이스 정보
        @Field("deviceInfo") deviceInfo: String,
        @Field("kakaoUserId") kakaoUserId: String,
        // Firebase uid
        @Field("uid") uid: String
    ): Call<APIResult<Any?>>
    // ---------------------------------------------------------------------------------------------

    // 자체 로그인 API
    @FormUrlEncoded
    @POST("api/v1/sopo-api/login/sopo")
    @Headers("Accept: application/json")
    fun requestSelfLogin(
        // 유저 이메일
        @Field("email") email: String,
        // 비밀번호
        @Field("password") pwd: String,
        // 디바이스 정보
        @Field("deviceInfo") deviceInfo: String,
        // 회원가입 타입
        @Field("joinType") joinType: String,
        // Firebase uid
        @Field("uid") uid: String
    ): Call<APIResult<Any?>>
    // ---------------------------------------------------------------------------------------------

    // 자동 로그인 API
    @FormUrlEncoded
    @POST("api/v1/sopo-api/login/auto")
    @Headers("Accept: application/json")
    fun requestAutoLogin(
        @Field("deviceInfo") deviceInfo: String,
        @Field("joinType") joinType: String,
        @Field("uid") uid: String,
        @Field("kakaoUserId") kakaoUserId : String?
    ): Call<APIResult<LoginResult?>>
}