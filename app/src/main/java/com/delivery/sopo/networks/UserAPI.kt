package com.delivery.sopo.networks

import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.LoginResult
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*

interface UserAPI
{

    // 카카오 로그인 시 Firebase 토큰으로 변경하는 Api
    @FormUrlEncoded
    @POST("api/v1/sopoMainBackEnd/login/kakao/verification")
    @Headers("Accept: application/json")
    fun requestCustomToken(
        @Field("deviceInfo") deviceInfo: String,
        @Field("email") email: String,
        @Field("joinType") joinType: String,
        @Field("userId") userId: String
    ): Observable<APIResult<String>>

    @FormUrlEncoded
    @POST("api/v1/sopoMainBackEnd/login/kakao")
    @Headers("Accept: application/json")
    fun requestKakaoLogin(
        @Field("email") email: String,
        @Field("authToken") token: String
    ): Observable<APIResult<LoginResult>>

    @FormUrlEncoded
    @POST("api/v1/sopoMainBackEnd/login/sopo")
    @Headers("Accept: application/json")
    fun requestSelfLogin(
        @Field("email") email: String,
        @Field("password") pwd: String,
        @Field("deviceInfo") deviceInfo: String,
        @Field("joinType") joinType: String
    ): Observable<APIResult<LoginResult>>

    @FormUrlEncoded
    @POST("api/v1/sopoMainBackEnd/login/auto")
    @Headers("Accept: application/json")
    fun requestAutoLogin(
        @Field("deviceInfo") deviceInfo: String,
        @Field("joinType") joinType: String
    ) : Single<APIResult<String>>

    @GET("api/v1/sopoMainBackEnd/validation/email/exist/{EMAIL}")
    @Headers("Accept: application/json")
    fun requestDuplicateEmail(
        @Path("EMAIL") email: String
    ): Observable<APIResult<Boolean>>

    @FormUrlEncoded
    @PATCH("/api/v1/sopoMainBackEnd/user/{email}/firebase/token")
    @Headers("Accept: application/json")
    fun updateFCMToken(
        @Path("email") email:String,
        @Field("firebaseToken") firebaseToken:String
    ) : Single<String>

}