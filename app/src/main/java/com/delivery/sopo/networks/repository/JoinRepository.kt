package com.delivery.sopo.networks.repository

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.call.JoinCall
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil

typealias DuplicateCallback = (SuccessResult<Boolean?>?, ErrorResult<Boolean?>?) -> Unit

object JoinRepository
{
    val deviceInfo = SOPOApp.deviceInfo

    private fun requestJoin(result: NetworkResult<APIResult<Unit>>): ResponseResult<Unit>
    {
        return when (result)
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data
                val code = CodeUtil.getCode(apiResult.code)
                ResponseResult(true, code, Unit, code.MSG)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val errorCode = exception.responseCode
                ResponseResult(false, errorCode, Unit, errorCode.MSG)
            }
        }
    }

    suspend fun requestJoinBySelf(email: String, password: String, nickname: String): ResponseResult<Unit>
    {
        val result = JoinCall.requestJoinBySelf(email, password, deviceInfo, nickname)
        return requestJoin(result)
    }

    suspend fun requestJoinByKakao(email: String, password: String, kakaoUid: String, nickname: String): ResponseResult<Unit>
    {
        val result = JoinCall.requestJoinByKakao(email, password, deviceInfo, kakaoUid, nickname)
        val res = requestJoin(result)

        if(res.code == ResponseCode.ALREADY_REGISTERED_USER)
        {
            return ResponseResult(true, res.code, Unit, res.message)
        }

        return res
    }

    suspend fun requestDuplicatedEmail(email: String, callback: DuplicateCallback)
    {
        when (val result = JoinCall.requestDuplicatedEmail(email))
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data
                val code = CodeUtil.getCode(apiResult.code)

                callback.invoke(SuccessResult(code, code.MSG, apiResult.data), null)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val errorCode = exception.responseCode

                callback.invoke(null, ErrorResult(errorCode, errorCode.MSG, ErrorResult.ERROR_TYPE_NON, false, exception))
            }
        }
    }
}
