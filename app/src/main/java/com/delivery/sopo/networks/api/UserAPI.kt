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
     * Nickname UPDATE
     * @param nickname : String
     * @return Response<APIResult<String?>>
     */
    @PATCH("/api/v1/sopo-api/user/nickname")
    suspend fun updateUserNickname(@Query("nickname") nickname : String) : Response<APIResult<String?>>

    /**
     * 탈퇴
     * @param reason : String
     * @return Response<APIResult<String?>>
     */
    @POST("/api/v1/sopo-api/user/signOut")
    @Headers("Accept: application/json")
    suspend fun requestSignOut(@Query("reason") reason : String) : Response<APIResult<String?>>
}
