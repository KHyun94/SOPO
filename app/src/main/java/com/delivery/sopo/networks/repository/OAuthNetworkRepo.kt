package com.delivery.sopo.networks.repository

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.mapper.OauthMapper
import com.delivery.sopo.models.OauthResult
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.networks.api.LoginAPICall
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

object OAuthNetworkRepo: KoinComponent
{
    private val userRepo: UserRepoImpl by inject()
    private val oAuthRepo: OauthRepoImpl by inject()

    // TODO 통합 필
    suspend fun loginWithOAuth(email: String, password: String): ResponseResult<OauthEntity?>
    {
        when(val result = LoginAPICall().requestOauth(email, password, SOPOApp.deviceInfo))
        {
            is NetworkResult.Success ->
            {
                userRepo.setEmail(email)
                userRepo.setApiPwd(password)

                val oAuth = Gson().let { gson ->
                    val type = object : TypeToken<OauthResult>() {}.type
                    val reader = gson.toJson(result.data)
                    val data = gson.fromJson<OauthResult>(reader, type)
                    OauthMapper.objectToEntity(data)
                }

                return ResponseResult(true, ResponseCode.SUCCESS, oAuth, ResponseCode.SUCCESS.MSG)
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

                userRepo.setNickname(apiResult.data?.nickname?:"")

                SopoLog.d("UserDetail >>> ${apiResult.data}, ${apiResult.data?.nickname}")

                return ResponseResult(true, ResponseCode.SUCCESS, apiResult.data, ResponseCode.SUCCESS.MSG)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val responseCode = exception.responseCode
                val date = SOPOApp.oAuthEntity.let { it?.expiresIn }

                return if(responseCode.HTTP_STATUS == 401 && DateUtil.isOverExpiredDate(date!!)) ResponseResult(false, responseCode, null, "로그인 기한이 만료되었습니다.\n다시 로그인해주세요.", DisplayEnum.DIALOG)
                else ResponseResult(false, responseCode, null, responseCode.MSG, DisplayEnum.DIALOG)
            }
        }
    }

}