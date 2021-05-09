package com.delivery.sopo.networks.api

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.LoginResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface LoginAPI
{

    @FormUrlEncoded
    @POST("api/v1/sopo-auth/oauth/token")
    @Headers("Accept: application/json")
    suspend fun requestOauth(
        // 유저 이메일
        @Field("grant_type") grantType: String,
        // 비밀번호
        @Field("username") email: String,
        // Firebase uid
        @Field("password") password: String,
        // 디바이스 정보
        @Field("device-info") deviceInfo: String
    ):Response<Any>
}
class LoginAPICall : BaseService()
{
    init
    {
        NetworkManager.setLogin(null, null)
    }

    suspend fun requestOauth(email: String,  password: String, deviceInfo: String): NetworkResult<Any>
    {
        NetworkManager.setLogin(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_PASSWORD)
        val requestOauth = NetworkManager.retro.create(LoginAPI::class.java).requestOauth(grantType = "password", email = email, password = password, deviceInfo = deviceInfo)

        return apiCall(call = {requestOauth})
    }
}