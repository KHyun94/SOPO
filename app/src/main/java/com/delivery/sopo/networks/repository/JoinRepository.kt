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

    private fun<T, E> requestJoin(result: NetworkResult<APIResult<T>>): ResponseResult<E>
    {
        return when (result)
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data
                val code = CodeUtil.getCode(apiResult.code)
                ResponseResult(true, code, apiResult.data as E, code.MSG)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val errorCode = exception.responseCode
                val apiResult = exception.apiResult
                val message = if (exception.apiResult != null)
                {
                    exception.apiResult?.message
                }
                else errorCode.MSG

                ResponseResult(false, errorCode, apiResult?.data as E, message ?: "알 수 없는 오류")
            }
        }
    }

    suspend fun requestJoinBySelf(email: String, password: String): ResponseResult<Unit>
    {
        val result = JoinCall.requestJoinBySelf(email, password, deviceInfo)
        return requestJoin<Unit, Unit>(result)
    }

    suspend fun requestJoinByKakao(email: String, password: String, kakaoUid: String, nickname: String?): ResponseResult<Unit>
    {
        val result = JoinCall.requestJoinByKakao(email, password, deviceInfo, kakaoUid, nickname)
        val res = requestJoin<Unit, Unit>(result)

        if(res.code == ResponseCode.ALREADY_REGISTERED_USER)
        {
            return ResponseResult(true, res.code, Unit, res.message)
        }

        return res
    }

    suspend fun requestDuplicatedEmail(email: String): ResponseResult<Boolean>
    {
        val result = JoinCall.requestDuplicatedEmail(email)
        return requestJoin<Boolean, Boolean>(result)
    }
}
