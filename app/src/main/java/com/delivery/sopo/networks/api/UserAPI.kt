package com.delivery.sopo.networks.api

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.Result
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface UserAPI
{
    // 카카오 로그인 시 Firebase 토큰으로 변경하는 Api ------------------------------------------------
    /*
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


     */

    //    @GET("api/v1/sopo-api/user-management/{email}/user/firebase/auth-token")
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

    @GET("api/v1/sopo-api/validation/email/exist/{email}")
    @Headers("Accept: application/json")
    fun requestDuplicateEmail(
        @Path("email") email: String
    ): Observable<APIResult<Boolean>>

    // 특정 값을 변경 및 삭제 등 수정 요청 api
    @PATCH("/api/v1/sopo-api/user/deviceInfo")
    fun patchUser(
        @Header("Content-Type") contentType: String = "application/json",
        @Query("jwtToken") jwtToken: String?,
        @Query("email") email: String,
        @Body jsonPatch: JsonPatchDto
    ): Call<APIResult<String?>>

    // 특정 값을 변경 및 삭제 등 수정 요청 api
    @PATCH("/api/v1/sopo-api/user/deviceInfo")
    fun test(
        @Header("Content-Type") contentType: String = "application/json",
        @Query("jwtToken") jwtToken: String,
        @Query("email") email: String,
        @Body jsonPatch: JsonPatchDto?
    ): Response<APIResult<String?>>
}

class UserAPICall : BaseService()
{
    private val userAPI = NetworkManager.publicRetro.create(UserAPI::class.java)

    suspend fun patchUser(
        email: String,
        jwtToken: String,
        jsonPatch: JsonPatchDto?
    ): Result<APIResult<String?>>
    {
        val patchUser = userAPI.test(email = email, jwtToken = jwtToken, jsonPatch = jsonPatch)
        return apiCall(call = { patchUser })
    }

    suspend fun requestCustomToken(
        email: String,
        deviceInfo: String,
        joinType: String,
        userId: String
    ): Result<APIResult<String?>>
    {
        val requestCustomToken = userAPI.requestCustomToken(
            email = email,
            deviceInfo = deviceInfo,
            joinType = joinType,
            userId = userId
        )
        return apiCall(call = { requestCustomToken })
    }
}

/**
 *     // 특정 값을 변경 및 삭제 등 수정 요청 api
@PATCH("/api/v1/sopo-api/user/deviceInfo")
fun patchUser(
@Header("Content-Type") contentType: String = "application/json",
@Query("jwtToken") jwtToken: String,
@Query("email") email: String,
@Body jsonPatch: JsonPatchDto?
): Response<APIResult<String?>>
}

class UserAPICall : BaseService()
{
private val userAPI = NetworkManager.publicRetro.create(UserAPI::class.java)

suspend fun patchUser(
email : String,
jwtToken: String,
jsonPatch: JsonPatchDto?
) : Result<APIResult<String?>>
{
val patchUser = userAPI.patchUser(email = email, jwtToken = jwtToken, jsonPatch = jsonPatch)
return apiCall(call = { patchUser })
}
 */