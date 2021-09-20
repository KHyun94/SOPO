package com.delivery.sopo.data.repository.remote.user

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.exceptions.NetworkException
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.models.PasswordResetDTO
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.networks.api.LoginAPICall
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserRemoteRepository:KoinComponent
{
    private val userLocalRepo: UserLocalRepository by inject()
    private val oAuthLocalRepo: OAuthLocalRepository by inject()
    private val gson: Gson = Gson()

    suspend fun requestLogin(email: String, password: String): ResponseResult<OAuthDTO>
    {
        when(val result = LoginAPICall().requestOauth(email, password.toMD5(), SOPOApp.deviceInfo))
        {
            is NetworkResult.Success ->
            {
                val type = object : TypeToken<OAuthDTO>() {}.type
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

                SOPOApp.oAuth = oAuthInfo

                return ResponseResult(result = true, code = ResponseCode.SUCCESS, data = oAuthInfo, message = ResponseCode.SUCCESS.MSG)
            }
            is NetworkResult.Error ->
            {
                val apiException = result.exception as APIException
                throw apiException
            }
        }
    }

    suspend fun getUserInfo(): ResponseResult<UserDetail>
    {
        when(val result = UserCall.getUserDetailInfo())
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data

                apiResult.data?.let {userDetail ->
                    userLocalRepo.setNickname(userDetail.nickname?:"")
                    userLocalRepo.setPersonalStatusType(userDetail.personalMessage.type)
                    userLocalRepo.setPersonalStatusMessage(userDetail.personalMessage.message)
                }

                return ResponseResult(true, ResponseCode.SUCCESS, apiResult.data!!, ResponseCode.SUCCESS.MSG)
            }
            is NetworkResult.Error ->
            {
                val apiException = result.exception as APIException
                throw apiException

                /*val exception = result.exception as APIException
                val responseCode = exception.responseCode
                val date = SOPOApp.oAuth.let { it?.expiresIn }

                return if(responseCode.HTTP_STATUS == 401 && DateUtil.isOverExpiredDate(date!!)) ResponseResult(false, responseCode, null, "로그인 기한이 만료되었습니다.\n다시 로그인해주세요.", DisplayEnum.DIALOG)
                else ResponseResult(false, responseCode, null, responseCode.MSG, DisplayEnum.DIALOG)*/
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
                val date = SOPOApp.oAuth.let { it?.expiresIn }

                return if(responseCode.HTTP_STATUS == 401 && DateUtil.isOverExpiredDate(date!!)) ResponseResult(false, responseCode, null, "로그인 기한이 만료되었습니다.\n다시 로그인해주세요.", DisplayEnum.DIALOG)
                else ResponseResult(false, responseCode, null, responseCode.MSG, DisplayEnum.DIALOG)
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