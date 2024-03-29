package com.delivery.sopo.networks.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface OAuthAPI
{
    /**
     * 자동 로그인 체크를 위한 AccessToken 유효성 검사 API
     * @param token OAuth Access Token\
     * @return OAuthResult
     * @throws APIResult<String>
     * */
    @FormUrlEncoded
    @POST("api/v1/sopo-auth/oauth/check_token")
    @Headers("Accept: application/json")
    suspend fun checkOAuthToken(
        // 유저 이메일
        @Field("token") accessToken : String
    ): Response<Any>

    /**
     * 기간이 만료된 OAuth Access Token을 갱신
     * @param grantType
     * @param email
     * @param refreshToken
     * @param deviceInfo
     * @return OAuthResult
     * @throws APIResult<String>
     * */
    @FormUrlEncoded
    @POST("api/v1/sopo-auth/oauth/token")
    @Headers("Accept: application/json")
    fun requestRefreshOAuthToken(
        // 유저 이메일
        @Field("grant_type") grantType: String,
        // 비밀번호
        @Field("user_id") email: String,
        // Firebase uid
        @Field("refresh_token") refreshToken : String,
        // 디바이스 정보
        @Field("deviceInfo") deviceInfo: String
    ): Call<Any>
}