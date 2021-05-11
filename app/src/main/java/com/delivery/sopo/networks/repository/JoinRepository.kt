package com.delivery.sopo.networks.repository

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.call.JoinCall
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog

object JoinRepository
{
    val deviceInfo = SOPOApp.deviceInfo

    private fun<T> requestJoin(result: NetworkResult<APIResult<T>>): ResponseResult<T>
    {
        return when (result)
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data
                val code = CodeUtil.getCode(apiResult.code)

                SopoLog.d("success to request >>> $apiResult $code")

                ResponseResult(true, code, apiResult.data as T, code.MSG)
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

                SopoLog.d("fail to request >>> $apiResult $message")

                ResponseResult(false, errorCode, apiResult?.data as T, message ?: "알 수 없는 오류")
            }
        }
    }

    suspend fun requestJoinBySelf(joinInfoDTO: JoinInfoDTO): ResponseResult<Unit>
    {
        SopoLog.d("requestJoinBySelf() call >>> $joinInfoDTO")
        val result = JoinCall.requestJoinBySelf(joinInfoDTO = joinInfoDTO)
        return requestJoin(result)
    }

    suspend fun requestJoinByKakao(joinInfoDTO: JoinInfoDTO): ResponseResult<Unit>
    {
        SopoLog.d("requestJoinByKakao() call >>> $joinInfoDTO")
        val result = JoinCall.requestJoinByKakao(joinInfoDTO = joinInfoDTO)
        val res = requestJoin(result)

        if(res.code == ResponseCode.ALREADY_REGISTERED_USER)
        {
            SopoLog.d("카카오 회원가입 >>> 이미 존재하는 아이디")
            return ResponseResult(true, res.code, Unit, res.message)
        }

        return res
    }

}
