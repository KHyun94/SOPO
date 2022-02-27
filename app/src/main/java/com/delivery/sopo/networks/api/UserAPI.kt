package com.delivery.sopo.networks.api

import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.user.ResetAuthCode
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
     */
    @GET("/api/v1/sopo-user/password/auth-info")
    @Headers("Accept: application/json")
    suspend fun requestSendTokenToEmail(
        @Query("email") email: String
    ) : Response<APIResult<String>>


    @POST("/api/v1/sopo-user/password/auth-info/verify")
    @Headers("Accept: application/json")
    suspend fun requestVerifyAuthToken(
            @Body resetAuthCode: ResetAuthCode
    ) : Response<Unit>

    /**
     * 비밀번호 리셋
     * @param PasswordResetDTO
     * @return Response<APIResult<String?>>
     */
    @POST("/api/v1/sopo-user/password/reset")
    @Headers("Accept: application/json")
    suspend fun requestResetPassword(@Body resetPassword: ResetPassword) : Response<APIResult<String?>>

    /**
     * 탈퇴
     * @param reason : String
     * @return Response<APIResult<String?>>
     */
    @POST("/api/v1/sopo-user/signOut")
    @Headers("Accept: application/json")
    suspend fun requestSignOut(@Query("reason") reason : String) : Response<APIResult<String?>>
}
