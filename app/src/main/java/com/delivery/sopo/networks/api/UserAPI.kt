package com.delivery.sopo.networks.api

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.google.gson.JsonArray
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface UserAPI
{
    // 카카오 로그인 시 Firebase 토큰으로 변경하는 Api ------------------------------------------------
    @GET("api/v1/sopo-api/user-management/{email}/user/firebase/auth-token")
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

    @GET("api/v1/sopo-api/validation/email/exist/{EMAIL}")
    @Headers("Accept: application/json")
    fun requestDuplicateEmail(
        @Path("EMAIL") email: String
    ): Observable<APIResult<Boolean>>

    @PATCH("/api/v1/sopo-api/user-management/{email}/user")
    fun patchUser(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("jwt") jwt: String?,
        @Path("email") email : String,
        @Body jsonPatch: JsonPatchDto
    ): Call<APIResult<String?>>
}