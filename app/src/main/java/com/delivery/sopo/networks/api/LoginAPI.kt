package com.delivery.sopo.networks.api

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.LoginResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.Result
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface LoginAPI
{
    // 자체 회원가입
    @FormUrlEncoded
    @POST("api/v1/sopo-api/join/sopo")
    @Headers("Accept: application/json")
    suspend fun requestSelfJoin(
        // 유저 이메일
        @Field("email") email: String,
        // 비밀번호
        @Field("password") pwd: String,
        // 디바이스 정보
        @Field("deviceInfo") deviceInfo: String,
        // Firebase uid
        @Field("uid") uid: String
    ):Response<APIResult<String>>

    // 카카오 회원가입
    @FormUrlEncoded
    @POST("api/v1/sopo-api/join/kakao")
    @Headers("Accept: application/json")
    suspend fun requestKakaoJoin(
        // 유저 이메일
        @Field("email") email: String,
        // 비밀번호
        @Field("password") pwd: String,
        // 디바이스 정보
        @Field("deviceInfo") deviceInfo: String,
        // Firebase uid
        @Field("kakaoUserId") kakaoUserId: String,
        // Firebase uid
        @Field("uid") uid: String
    ):Response<APIResult<String>>

    @FormUrlEncoded
    @POST("api/v1/sopo-auth/oauth/token")
    @Headers("Accept: application/json")
    suspend fun requestOauth(
        // 유저 이메일
        @Field("grant_type") grantType: String,
        // 비밀번호
        @Field("username") email: String,
        // Firebase uid
        @Field("password") password: String,
        // 디바이스 정보
        @Field("device-info") deviceInfo: String
    ):Response<Any>

    @FormUrlEncoded
    @POST("api/v1/sopo-auth/oauth/token")
    @Headers("Accept: application/json")
    suspend fun requestRefresh(
        // 유저 이메일
        @Field("grant_type") grantType: String,
        // 비밀번호
        @Field("username") email: String,
        // Firebase uid
        @Field("refresh_token") refreshToken : String,
        // 디바이스 정보
        @Field("deviceInfo") deviceInfo: String
    ):Response<Any>

    //----------------------------------------------------------------------------------------------
    // 카카오 간편 로그인
    /*
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
    */

    @FormUrlEncoded
    @POST("api/v1/sopo-api/login/kakao")
    @Headers("Accept: application/json")
    suspend fun requestKakaoLogin(
        // 이메일
        @Field("email") email: String,
        // 디바이스 정보
        @Field("deviceInfo") deviceInfo: String,
        @Field("kakaoUserId") kakaoUserId: String,
        // Firebase uid
        @Field("uid") uid: String
    ): Response<APIResult<Any?>>

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
class LoginAPICall : BaseService()
{
    val loginAPI = NetworkManager.joinRetro.create(LoginAPI::class.java)

    suspend fun requestKakaoLogin(email: String, deviceInfo: String, kakaoUserId: String, uid: String): Result<APIResult<Any?>>
    {
        val requestKakaoLogin = loginAPI.requestKakaoLogin(email = email, deviceInfo = deviceInfo, kakaoUserId = kakaoUserId, uid = uid)
        return apiCall(call = {requestKakaoLogin})
    }

    suspend fun requestSelfJoin(email: String, deviceInfo: String, password: String, uid: String): Result<APIResult<String>>
    {
        val requestSelfJoin = loginAPI.requestSelfJoin(email = email, pwd = password, deviceInfo = deviceInfo, uid = uid)
        return apiCall(call = {requestSelfJoin})
    }

    suspend fun requestKakaoJoin(email: String, password: String, deviceInfo: String, kakaoUserId: String,  uid: String): Result<APIResult<String>>
    {
        val requestKakapJoin = loginAPI.requestKakaoJoin(email = email, pwd = password, deviceInfo = deviceInfo, kakaoUserId = kakaoUserId, uid = uid)
        return apiCall(call = {requestKakapJoin})
    }

    suspend fun requestOauth(email: String, deviceInfo: String, password: String): Result<Any>
    {
        val requestOauth = NetworkManager.oauthRetro.create(LoginAPI::class.java).requestOauth(grantType = "password", email = email, password = password, deviceInfo = deviceInfo)
        return apiCall(call = {requestOauth})
    }

    suspend fun requestRefreshOauth(email: String, deviceInfo: String, refreshToken: String): Result<Any>
    {
        val requestOauth = NetworkManager.oauthRetro.create(LoginAPI::class.java).requestRefresh(grantType = "refresh_token", email = email, refreshToken = refreshToken, deviceInfo = deviceInfo)
        return apiCall(call = {requestOauth})
    }
}