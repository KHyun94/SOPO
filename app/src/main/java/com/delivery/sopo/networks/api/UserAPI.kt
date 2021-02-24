package com.delivery.sopo.networks.api

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface UserAPI
{
    @GET("api/v1/sopo-api/user/firebase/auth-token")
    @Headers("Accept: application/json")
    suspend fun requestCustomToken(
        // 이메일
        @Query("email") email: String,
        // 디바이스 정보
        @Query("deviceInfo") deviceInfo: String,
        // 회원가입 타입
        @Query("joinType") joinType: String,
        // 현 카카오 유니크 아이디
        @Query("userId") userId: String
    ): Response<APIResult<String?>>

    /** 기존 발행된 access Token의 유효성 검사 */
    @POST("api/v1/sopo-auth/oauth/check_token")
    @Headers("Accept: application/json")
    suspend fun checkOAuthToken(@Field("token") token : String) : Response<Any>

    // todo 위의 api로 변경 예정
    // 특정 값을 변경 및 삭제 등 수정 요청 api
    @PATCH("/api/v1/sopo-api/user/deviceInfo")
    suspend fun test(
        @Header("Content-Type") contentType: String = "application/json",
        @Query("jwtToken") jwtToken: String,
        @Query("email") email: String,
        @Body jsonPatch: JsonPatchDto?
    ): Response<APIResult<String?>>

    /**
     * 자동 로그인 및 유저 데이터 가져오기
     * @return Response<APIResult<UserDetail?>>
     */
    @GET("/api/v1/sopo-api/user/detail")
    @Headers("Accept: application/json")
    suspend fun getUserInfoWithToken() : Response<APIResult<UserDetail?>>

    /**
     * FCM Token UPDATE
     * @param fcmToken : String
     * @return Response<APIResult<String?>>
     */
    @PATCH("/api/v1/sopo-api/user/fcmToken")
    suspend fun updateFCMToken(@Query("fcmToken") fcmToken : String) : Response<APIResult<String?>>

    /**
     * FCM Token UPDATE
     * @param nickname : String
     * @return Response<APIResult<String?>>
     */
    @PATCH("/api/v1/sopo-api/user/nickName")
    suspend fun updateUserNickname(@Query("nickName") nickname : String) : Response<APIResult<String?>>
}