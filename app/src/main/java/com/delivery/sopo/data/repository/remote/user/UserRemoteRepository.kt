package com.delivery.sopo.data.repository.remote.user

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIBetaException
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.extensions.toMD5
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
import com.delivery.sopo.services.network_handler.NetworkResponseBeta
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.google.firebase.auth.UserInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserRemoteRepository:KoinComponent, BaseServiceBeta()
{
    private val userLocalRepo: UserLocalRepository by inject()
    private val oAuthLocalRepo: OAuthLocalRepository by inject()
    private val gson: Gson = Gson()

    suspend fun requestLogin(email: String, password: String)
    {
        val requestOAuthToken = NetworkManager.retro(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_PASSWORD).create(OAuthAPI::class.java).requestQAuthToken(grantType = "password", email = email, password = password.toMD5())

        when(val result = apiCall(call = { requestOAuthToken }))
        {
            is NetworkResponseBeta.Success ->
            {
                val type = object : TypeToken<OAuthDTO>() {}.type
                val reader = gson.toJson(result.data)
                val oAuthInfo = gson.fromJson<OAuthDTO>(reader, type)

                userLocalRepo.run {
                    setUserId(email)
                    setUserPassword(password.toMD5())
                    setStatus(StatusConst.ACTIVATE)
                }

                withContext(Dispatchers.Default) {
                    oAuthLocalRepo.insert(dto = oAuthInfo)
                }
            }
            is NetworkResponseBeta.SuccessNoBody ->
            {
                throw InternalError("일시적으로 조회가 불가능합니다. 다시 확인 부탁드리겠습니다.")
            }
            is NetworkResponseBeta.Error ->
            {
                throw APIBetaException(result.statusCode, result.errorResponse)
            }
        }
    }

    suspend fun getUserInfo(): UserDetail
    {
        val getUserInfo = NetworkManager.setLoginMethod(NetworkEnum.O_AUTH_TOKEN_LOGIN, UserAPI::class.java).getUserDetailInfo()

        when(val result = apiCall(call = { getUserInfo }))
        {
            is NetworkResponseBeta.Success ->
            {
                val apiResult = result.data

                val userInfo = apiResult.data ?: throw APIBetaException(200, ErrorResponse(401, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))

                withContext(Dispatchers.Default){
                    userLocalRepo.run {
                        setNickname(userInfo.nickname?:"")
                        setPersonalStatusType(userInfo.personalMessage.type)
                        setPersonalStatusMessage(userInfo.personalMessage.message)
                    }
                }

                return userInfo
            }
            is NetworkResponseBeta.SuccessNoBody ->
            {
                throw InternalError("일시적으로 조회가 불가능합니다. 다시 확인 부탁드리겠습니다.")
            }
            is NetworkResponseBeta.Error ->
            {
                throw APIBetaException(result.statusCode, result.errorResponse)
            }
        }
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

    suspend fun requestPasswordForReset(passwordResetDTO: PasswordResetDTO): ResponseResult<Unit>{
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

    suspend fun updateFCMToken(fcmToken: String): ResponseResult<Unit>{

        val fcmTokenToMap = mapOf(Pair("fcmToken", fcmToken))

        when(val result = UserCall.updateFCMToken(fcmToken = fcmTokenToMap))
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data

                SopoLog.d("Success to update fcm token")

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

    suspend fun updateNickname(nickname: String): ResponseResult<String>
    {
        return when(val result = UserCall.updateNickname(nickname))
        {
            is NetworkResult.Success ->
            {
                SopoLog.d("Success to update nickname")
                val apiResult = result.data
                val resCode = CodeUtil.getCode(apiResult.code)
                ResponseResult(true, resCode , nickname, "Success to update nickname")
            }
            is NetworkResult.Error ->
            {
                SopoLog.e("Fail to update nickname")

                val exception = result.exception as APIException
                val responseCode = exception.responseCode

                ResponseResult(false, responseCode, "", "Fail to update nickname", DisplayEnum.DIALOG)
            }
        }
    }

}