package com.delivery.sopo.networks.api

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.models.PasswordResetDTO
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface UserAPI
{
    /**
     * 자동 로그인 및 유저 데이터 가져오기
     * @return Response<APIResult<UserDetail?>>
     */
    @GET("/api/v1/sopo-user/detail")
    @Headers("Accept: application/json")
    suspend fun getUserDetailInfo() : Response<APIResult<UserDetail>>

    /**
     * FCM Token UPDATE
     * @param fcmToken : String
     * @return Response<APIResult<String?>>
     */
    @PATCH("/api/v1/sopo-user/fcmToken")
    suspend fun updateFCMToken(@Body fcmToken : Map<String, String>) : Response<Unit>

    /**
     * Nickname UPDATE
     * @param nickname : String
     * @return Response<APIResult<String?>>
     */
    @PATCH("/api/v1/sopo-user/nickname")
    suspend fun updateUserNickname(@Body nickname : Map<String, String>) : Response<Unit>

    /**
     * Send Email For request PIN CODE
     * @param nickname : String
     * @return Response<APIResult<String?>>
     *
     * 의문
     * 1. GET?
     * 2. parameter는 필요 없는지
     */
    @GET("/api/v1/sopo-api/user/password/auth-info")
    @Headers("Accept: application/json")
    suspend fun requestEmailForAuth(
        @Query("email") email: String
    ) : Response<APIResult<EmailAuthDTO?>>

    /**
     * 비밀번호 리셋
     * @param PasswordResetDTO
     * @return Response<APIResult<String?>>
     */
    @POST("/api/v1/sopo-api/user/password/reset")
    @Headers("Accept: application/json")
    suspend fun requestResetPassword(@Body passwordResetDTO: PasswordResetDTO) : Response<APIResult<String?>>

    /**
     * 탈퇴
     * @param reason : String
     * @return Response<APIResult<String?>>
     */
    @POST("/api/v1/sopo-api/user/signOut")
    @Headers("Accept: application/json")
    suspend fun requestSignOut(@Query("reason") reason : String) : Response<APIResult<String?>>
}
