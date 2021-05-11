package com.delivery.sopo.data.repository.remote.o_auth

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.networks.api.LoginAPICall
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.core.KoinComponent
import org.koin.core.inject

object OAuthRemoteRepository: KoinComponent
{
    private val userLocalRepo: UserLocalRepository by inject()

    suspend fun requestLoginWithOAuth(email: String, password: String): ResponseResult<OAuthDTO?>
    {
        when(val result = LoginAPICall().requestOauth(email, password, SOPOApp.deviceInfo))
        {
            is NetworkResult.Success ->
            {
                val oAuthDTO = Gson().let { gson ->
                    val type = object : TypeToken<OAuthDTO>() {}.type
                    val reader = gson.toJson(result.data)
                    gson.fromJson<OAuthDTO>(reader, type)
                }

                return ResponseResult(true, ResponseCode.SUCCESS, oAuthDTO, ResponseCode.SUCCESS.MSG)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val code = exception.responseCode
                return ResponseResult(false, code, null, code.MSG, DisplayEnum.DIALOG)
            }
        }
    }

    suspend fun getUserInfo(): ResponseResult<UserDetail?>
    {
        when(val result = UserCall.getUserInfoWithToken())
        {
            is NetworkResult.Success ->
            {
                val apiResult = (result as NetworkResult.Success).data

                userLocalRepo.setNickname(apiResult.data?.nickname?:"")

                SopoLog.d("UserDetail >>> ${apiResult.data}, ${apiResult.data?.nickname}")

                return ResponseResult(true, ResponseCode.SUCCESS, apiResult.data, ResponseCode.SUCCESS.MSG)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val responseCode = exception.responseCode
                val date = SOPOApp.oAuth.let { it?.expiresIn }

                return if(responseCode.HTTP_STATUS == 401 && DateUtil.isOverExpiredDate(date!!)) ResponseResult(false, responseCode, null, "로그인 기한이 만료되었습니다.\n다시 로그인해주세요.", DisplayEnum.DIALOG)
                else ResponseResult(false, responseCode, null, responseCode.MSG, DisplayEnum.DIALOG)
            }
        }
    }

    suspend fun requestEmailForAuth(): ResponseResult<String?>
    {
        when(val result = UserCall.requestEmailForAuth())
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
}