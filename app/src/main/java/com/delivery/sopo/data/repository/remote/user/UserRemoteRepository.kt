package com.delivery.sopo.data.repository.remote.user

import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.models.PasswordResetDTO
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog

class UserRemoteRepository
{
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