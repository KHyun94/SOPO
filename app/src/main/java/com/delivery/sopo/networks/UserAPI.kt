package com.delivery.sopo.networks

import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.LoginResult
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface UserAPI
{
    // 카카오 로그인 시 Firebase 토큰으로 변경하는 Api ------------------------------------------------
    @GET("api/v1/sopoMainBackEnd/user/{email}/firebase/auth-token")
    @Headers("Accept: application/json")
    fun requestCustomToken(
        // 이메일
        @Path("email") email: String,
        // 디바이스 정보
        @Query("deviceInfo") deviceInfo: String,
        // 회원가입 타입
        @Query("joinType") joinType: String,
        // 현 카카오 유니크 아이디
        @Query("userId") userId: String
    ): Call<APIResult<String?>>

    @FormUrlEncoded
    @PATCH("api/v1/sopoMainBackEnd/user/{email}/deviceInfo")
    @Headers("Accept: application/json")
    fun requestUpdateDeviceInfo(
        @Path("email") email: String,
        @Field("jwtToken") jwtToken: String
    ): Call<APIResult<String?>>

    @GET("api/v1/sopoMainBackEnd/validation/email/exist/{EMAIL}")
    @Headers("Accept: application/json")
    fun requestDuplicateEmail(
        @Path("EMAIL") email: String
    ): Observable<APIResult<Boolean>>

    @FormUrlEncoded
    @PATCH("/api/v1/sopoMainBackEnd/user/{email}/firebase/fcm-token")
    @Headers("Accept: application/json")
    fun updateFCMToken(
        @Path("email") email: String,
        @Field("fcmToken") fcmToken: String
    ): Single<String>
}