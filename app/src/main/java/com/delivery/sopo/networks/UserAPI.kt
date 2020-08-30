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

    // 카카오 간편 로그인
    @FormUrlEncoded
    @POST("api/v1/sopoMainBackEnd/login/kakao")
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
    @POST("api/v1/sopoMainBackEnd/login/sopo")
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

    @FormUrlEncoded
    @PATCH("api/v1/sopoMainBackEnd/user/{email}/deviceInfo")
    @Headers("Accept: application/json")
    fun requestUpdateDeviceInfo(
        @Path("email") email: String,
        @Field("jwtToken") jwtToken: String
    ): Call<APIResult<String?>>

    @FormUrlEncoded
    @POST("api/v1/sopoMainBackEnd/login/auto")
    @Headers("Accept: application/json")
    fun requestAutoLogin(
        @Field("deviceInfo") deviceInfo: String,
        @Field("joinType") joinType: String,
        @Field("uid") uid: String,
        @Field("kakaoUserId") kakaoUserId : String?
    ): Call<APIResult<LoginResult?>>

//    @FormUrlEncoded
//    @POST("api/v1/sopoMainBackEnd/login/kakao")
//    @Headers("Accept: application/json")
//    fun requestKakaoLogin(
//        @Field("email") email: String,
//        @Field("authToken") token: String
//    ): Call<APIResult<LoginResult>>


//    @FormUrlEncoded
//    @POST("api/v1/sopoMainBackEnd/login/auto")
//    @Headers("Accept: application/json")
//    fun requestAutoLogin(
//        @Field("deviceInfo") deviceInfo: String,
//        @Field("joinType") joinType: String,
//        @Field("userId") userId: String?
//    ): Call<APIResult<String>>

//    @GET("api/v1/sopoMainBackEnd/validation/email/exist/{EMAIL}")
//    @Headers("Accept: application/json")
//    fun requestDuplicateEmail(
//        @Path("EMAIL") email: String
//    ): Call<APIResult<Boolean>>

//    @FormUrlEncoded
//    @PATCH("/api/v1/sopoMainBackEnd/user/{email}/firebase/token")
//    @Headers("Accept: application/json")
//    fun updateFCMToken(
//        @Path("email") email: String,
//        @Field("firebaseToken") firebaseToken: String
//    ): Call<String>


//    // 카카오 로그인 시 Firebase 토큰으로 변경하는 Api
//    @FormUrlEncoded
//    @POST("api/v1/sopoMainBackEnd/login/kakao/verification")
//    @Headers("Accept: application/json")
//    fun requestCustomToken(
//        @Field("deviceInfo") deviceInfo: String,
//        @Field("email") email: String,
//        @Field("joinType") joinType: String,
//        @Field("userId") userId: String
//    ): Observable<APIResult<String>>


//    // 자체 로그인 API
//    @FormUrlEncoded
//    @POST("api/v1/sopoMainBackEnd/login/sopo")
//    @Headers("Accept: application/json")
//    fun requestSelfLogin(
//        @Field("email") email: String,
//        @Field("password") pwd: String,
//        @Field("deviceInfo") deviceInfo: String,
//        @Field("joinType") joinType: String,
//        @Field("uid") uid:String
//    ): Observable<APIResult<LoginResult>>

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
        @Field("firebaseToken") firebaseToken: String
    ): Single<String>

}