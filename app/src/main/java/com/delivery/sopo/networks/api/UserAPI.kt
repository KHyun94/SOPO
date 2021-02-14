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

    // 특정 값을 변경 및 삭제 등 수정 요청 api
    @PATCH("/api/v1/sopo-api/user/deviceInfo")
    fun patchUser(
        @Header("Content-Type") contentType: String = "application/json",
        @Query("jwtToken") jwtToken: String?,
        @Query("email") email: String,
        @Body jsonPatch: JsonPatchDto
    ): Call<APIResult<String?>>

    // todo 위의 api로 변경 예정
    // 특정 값을 변경 및 삭제 등 수정 요청 api
    @PATCH("/api/v1/sopo-api/user/deviceInfo")
    suspend fun test(
        @Header("Content-Type") contentType: String = "application/json",
        @Query("jwtToken") jwtToken: String,
        @Query("email") email: String,
        @Body jsonPatch: JsonPatchDto?
    ): Response<APIResult<String?>>

    @GET("/api/v1/sopo-api/user/detail")
    @Headers("Accept: application/json")
    suspend fun getUserInfoWithToken() : Response<APIResult<UserDetail?>>


}

class UserAPICall : BaseService()
{
    init
    {
        NetworkManager.setLogin(BuildConfig.PUBLIC_API_ACCOUNT_ID, BuildConfig.PUBLIC_API_ACCOUNT_PASSWORD)
    }

    private val userAPI = NetworkManager.retro.create(UserAPI::class.java)

    suspend fun patchUser(email: String, jwtToken: String, jsonPatch: JsonPatchDto?): NetworkResult<APIResult<String?>>
    {
        val patchUser = userAPI.test(email = email, jwtToken = jwtToken, jsonPatch = jsonPatch)
        return apiCall(call = { patchUser })
    }

    suspend fun requestCustomToken(
        email: String,
        deviceInfo: String,
        joinType: String,
        userId: String
    ): NetworkResult<APIResult<String?>>
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
