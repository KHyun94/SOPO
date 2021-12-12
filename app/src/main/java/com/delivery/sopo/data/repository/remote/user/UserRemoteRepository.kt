package com.delivery.sopo.data.repository.remote.user

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.models.PasswordResetDTO
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.OAuthAPI
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.services.network_handler.BaseServiceBeta
import com.delivery.sopo.services.network_handler.NetworkResponse
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserRemoteRepository: KoinComponent, BaseServiceBeta()
{
    private val userLocalRepo: UserLocalRepository by inject()
    private val oAuthLocalRepo: OAuthLocalRepository by inject()
    private val gson: Gson = Gson()

    suspend fun requestLogin(email: String, password: String)
    {
        val requestOAuthToken = NetworkManager.retro(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_PASSWORD).create(OAuthAPI::class.java).requestQAuthToken(grantType = "password", email = email, password = password)

        val result = apiCall { requestOAuthToken }

        val type = object: TypeToken<OAuthDTO>() {}.type
        val reader = gson.toJson(result.data)
        val oAuthInfo = gson.fromJson<OAuthDTO>(reader, type)

        userLocalRepo.run {
            setUserId(email)
            setUserPassword(password)
            setStatus(StatusConst.ACTIVATE)
        }

        withContext(Dispatchers.Default) {
            oAuthLocalRepo.insert(dto = oAuthInfo)
        }
    }

    suspend fun refreshOAuthToken(): OAuthDTO
    {
        SopoLog.d("refreshOAuthToken() 호출 ")
        val oAuthDTO: OAuthDTO = oAuthLocalRepo.get(userLocalRepo.getUserId())

        val refreshOAuthToken = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, OAuthAPI::class.java).requestRefreshOAuthToken(grantType = "refresh_token", email = userLocalRepo.getUserId(), refreshToken = oAuthDTO.refreshToken)

        val result = apiCall { refreshOAuthToken }

        val type = object: TypeToken<OAuthDTO>() {}.type
        val reader = gson.toJson(result.data)
        val oAuthInfo = gson.fromJson<OAuthDTO>(reader, type)

        withContext(Dispatchers.Default) {
            SopoLog.d("결과 ${oAuthInfo.toString()}")
            oAuthLocalRepo.insert(dto = oAuthInfo)
        }

        return oAuthInfo
    }

    suspend fun getUserInfo(): UserDetail
    {
        val getUserInfo =
            NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java)
                .getUserDetailInfo()

        val result = apiCall { getUserInfo }

        val userInfo = result.data?.data
            ?: throw SOPOApiException(200, ErrorResponse(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))

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
        val updateNickname = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java).updateUserNickname(nickname = mapOf<String, String>(Pair("nickname", nickname)))
        apiCall { updateNickname }
    }

    suspend fun requestSignOut(reason: String): ResponseResult<String?>
    {
        when(val result = UserCall.requestSignOut(reason))
        {
            is NetworkResult.Success ->
            {
                val apiResult = (result as NetworkResult.Success).data

                SopoLog.d("Success to sign out")

                return ResponseResult(true, ResponseCode.SUCCESS, apiResult.data, ResponseCode.SUCCESS.MSG)
            }
            is NetworkResult.Error ->
            {
                SopoLog.d("Fail to sign out")

                val exception = result.exception as APIException
                val responseCode = exception.responseCode
                //                val date = SOPOApp.oAuth.let { it?.expiresIn }
                //
                //                return if(responseCode.HTTP_STATUS == 401 && DateUtil.isOverExpiredDate(date!!)) ResponseResult(false, responseCode, null, "로그인 기한이 만료되었습니다.\n다시 로그인해주세요.", DisplayEnum.DIALOG)
                //                else ResponseResult(false, responseCode, null, responseCode.MSG, DisplayEnum.DIALOG)
                return ResponseResult(false, responseCode, null, responseCode.MSG, DisplayEnum.DIALOG)
            }
        }
    }

    suspend fun requestEmailForAuth(email: String): ResponseResult<EmailAuthDTO?>
    {
        when(val result = UserCall.requestEmailForAuth(email = email))
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data

                SopoLog.d("Success to request email for auth")

                return ResponseResult(true, ResponseCode.SUCCESS, apiResult.data, ResponseCode.SUCCESS.MSG)
            }
            is NetworkResult.Error ->
            {
                SopoLog.d("Fail to request email for auth")

                val exception = result.exception as APIException
                val responseCode = exception.responseCode

                return ResponseResult(false, responseCode, null, responseCode.MSG, DisplayEnum.DIALOG)
            }
        }
    }

    suspend fun requestPasswordForReset(passwordResetDTO: PasswordResetDTO): ResponseResult<Unit>
    {
        when(val result = UserCall.requestResetPassword(passwordResetDTO = passwordResetDTO))
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data

                SopoLog.d("Success to request reset password")

                return ResponseResult(true, ResponseCode.SUCCESS, Unit, ResponseCode.SUCCESS.MSG)
            }
            is NetworkResult.Error ->
            {
                SopoLog.d("Fail to request reset password")

                val exception = result.exception as APIException
                val responseCode = exception.responseCode

                return ResponseResult(false, responseCode, Unit, responseCode.MSG, DisplayEnum.DIALOG)
            }
        }
    }

    suspend fun updateFCMToken(fcmToken: String)
    {
        val fcmTokenToMap = mapOf(Pair("fcmToken", fcmToken))

        val updateFCMToken = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java).updateFCMToken(fcmToken = fcmTokenToMap)
        val result = apiCall { updateFCMToken }.apply {
            SopoLog.d("FCM Token 업데이트 성공")
        }
    }


}