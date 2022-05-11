package com.delivery.sopo.data.repository.remote.user

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.extensions.wrapBodyAliasToHashMap
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.data.networks.NetworkManager
import com.delivery.sopo.data.networks.serivces.OAuthAPI
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserRemoteRepository: KoinComponent, BaseService()
{
    private val userLocalRepo: UserLocalRepository by inject()
    private val oAuthLocalRepo: OAuthLocalRepository by inject()
    private val gson: Gson = Gson()

    suspend fun requestLogin(email: String, password: String)
    {
        val requestOAuthToken = NetworkManager.retro(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_PASSWORD).create(OAuthAPI::class.java).requestQAuthToken(grantType = "password", email = email, password = password)

        val result = apiCall { requestOAuthToken }

        val type = object: TypeToken<OAuthToken>() {}.type
        val reader = gson.toJson(result.data)
        val oAuthInfo = gson.fromJson<OAuthToken>(reader, type)

        userLocalRepo.run {
            setUserId(email)
            setUserPassword(password)
            setStatus(StatusConst.ACTIVATE)
        }

        withContext(Dispatchers.Default) {
            oAuthLocalRepo.insert(token = oAuthInfo)
        }
    }

    suspend fun refreshOAuthToken(): OAuthToken
    {
        SopoLog.d("refreshOAuthToken() 호출 ")
        val oAuthToken: OAuthToken = oAuthLocalRepo.get(userLocalRepo.getUserId())
        SopoLog.d("base o-auth token [email:${userLocalRepo.getUserId()}] [data:${oAuthToken.toString()}] ")
        val refreshOAuthToken = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, OAuthAPI::class.java).refreshToken(grantType = "refresh_token", email = userLocalRepo.getUserId(), refreshToken = oAuthToken.refreshToken)

        SopoLog.d("o-auth refresh 요청 전 ${refreshOAuthToken.errorBody()?.toString()}")

        val result = apiCall { refreshOAuthToken }

        SopoLog.d("before deserialize o-auth token [data:${result.data?.toString()}] ")

        val type = object: TypeToken<OAuthToken>() {}.type
        val reader = gson.toJson(result.data)
        val oAuthInfo = gson.fromJson<OAuthToken>(reader, type)

        SopoLog.d("after deserialize o-auth token [data:${oAuthInfo.toString()}] ")

        withContext(Dispatchers.Default) {
            SopoLog.d("결과 ${oAuthInfo.toString()}")
            oAuthLocalRepo.insert(token = oAuthInfo)
        }

        return oAuthInfo
    }

    suspend fun getUserInfo(): UserDetail
    {
        val getUserInfo = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserService::class.java).fetchUserInfo()

        val result = apiCall { getUserInfo }

        val userInfo = result.data?.data ?: throw SOPOApiException(200, ErrorResponse(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))

        withContext(Dispatchers.Default) {
            userLocalRepo.run {
                setNickname(userInfo.nickname ?: "")
                setPersonalStatusType(userInfo.personalMessage.type)
                setPersonalStatusMessage(userInfo.personalMessage.message)
            }
        }

        return userInfo
    }

    suspend fun updateNickname(nickname: String) = withContext(Dispatchers.IO)
    {
        val updateNickname = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserService::class.java).updateNickname(nickname = mapOf<String, String>(Pair("nickname", nickname)))
        apiCall { updateNickname }
    }

    suspend fun requestSignOut(reason: String) = withContext(Dispatchers.IO)
    {
        val wrap = reason.wrapBodyAliasToHashMap("reason")
        val signOut = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserService::class.java).deleteUser(reason = wrap)
        apiCall { signOut }
    }

    suspend fun requestSendTokenToEmail(email: String): String
    {
        val requestEmailForAuth = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, UserService::class.java).requestAuthCodeEmail(email = email)
        val result = apiCall { requestEmailForAuth }
        return result.data?.data?: throw SOPOApiException(404, ErrorResponse(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))
    }

    suspend fun requestVerifyAuthToken(resetAuthCode: ResetAuthCode)
    {
        val requestEmailForAuth = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, UserService::class.java).requestVerifyAuthToken(authCode = resetAuthCode)
        apiCall { requestEmailForAuth }
    }

    suspend fun requestResetPassword(resetPassword: ResetPassword)
    {
        val requestEmailForAuth = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, UserService::class.java).requestResetPassword(resetPassword = resetPassword)
        apiCall { requestEmailForAuth }
    }

    suspend fun updateFCMToken(fcmToken: String)
    {
        val fcmTokenToMap = mapOf(Pair("fcmToken", fcmToken))

        val updateFCMToken = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserService::class.java).updateFCMToken(fcmToken = fcmTokenToMap)
        val result = apiCall { updateFCMToken }.apply {
            SopoLog.d("FCM Token 업데이트 성공")
        }
    }


}