package com.delivery.sopo.networks.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface OAuthAPI
{

    @FormUrlEncoded
    @POST("api/v1/sopo-auth/oauth/token")
    @Headers("Accept: application/json")
    suspend fun requestQAuthToken(
            @Field("grant_type") grantType: String,
            @Field("username") email: String,
            @Field("password") password: String
    ):Response<Any>

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
    suspend fun requestRefreshTokenInOAuth(
            @Field("grant_type") grantType: String,
            @Field("user_id") email: String,
            @Field("refresh_token") refreshToken : String
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
        @Field("grant_type") grantType: String,
        @Field("user_id") email: String,
        @Field("refresh_token") refreshToken : String
    ): Response<Any>
}