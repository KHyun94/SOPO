package com.delivery.sopo.data.networks.serivces

import com.delivery.sopo.data.networks.NetworkManager
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.user.ResetAuthCode
import retrofit2.Response
import retrofit2.http.*

interface UserService
{
    @FormUrlEncoded
    @POST("api/v1/sopo-auth/oauth/token")
    @Headers("Accept: application/json")
    suspend fun issueToken(
            @Field("grant_type") grantType: String,
            @Field("username") userName: String,
            @Field("password") password: String
    ):Response<Any>

    /**
     * 기간이 만료된 OAuth Access Token을 갱신
     * @param grantType
     * @param userName
     * @param refreshToken
     * @param deviceInfo
     * @return OAuthResult
     * @throws APIResult<String>
     * */
    @FormUrlEncoded
    @POST("api/v1/sopo-auth/oauth/token")
    @Headers("Accept: application/json")
    suspend fun refreshToken(
            @Field("grant_type") grantType: String,
            @Field("user_id") userName: String,
            @Field("refresh_token") refreshToken : String
    ): Response<Any>

    /**
     * 자동 로그인 및 유저 데이터 가져오기
     * @return Response<APIResult<UserDetail?>>
     */
    @GET("/api/v1/sopo-user/detail")
    @Headers("Accept: application/json")
    suspend fun fetchUserInfo() : Response<APIResult<UserDetail>>

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
    suspend fun updateNickname(@Body nickname : Map<String, String>) : Response<Unit>

    /**
     * Send Email For request PIN CODE
     * @param nickname : String
     * @return Response<APIResult<String?>>
     */
    @GET("/api/v1/sopo-user/password/auth-info")
    @Headers("Accept: application/json")
    suspend fun requestAuthCodeEmail(
        @Query("email") email: String
    ) : Response<APIResult<String>>


    @POST("/api/v1/sopo-user/password/auth-info/verify")
    @Headers("Accept: application/json")
    suspend fun requestVerifyAuthToken(
            @Body authCode: ResetAuthCode
    ) : Response<Unit>

    /**
     * 비밀번호 리셋
     * @param PasswordResetDTO
     * @return Response<APIResult<String?>>
     */
    @POST("/api/v1/sopo-user/password/reset")
    @Headers("Accept: application/json")
    suspend fun updatePassword(@Body resetPassword: ResetPassword) : Response<APIResult<String?>>

    /**
     * 탈퇴
     * @param reason : String
     * @return Response<APIResult<String?>>
     */
    @POST("/api/v1/sopo-user/sign-out")
    @Headers("Accept: application/json")
    suspend fun deleteUser(@Body reason: Map<String, String>) : Response<Unit>

    companion object{
        fun create(networkType: NetworkEnum): UserService
        {
            return NetworkManager.setLoginMethod(networkType, UserService::class.java)
        }
    }
}
