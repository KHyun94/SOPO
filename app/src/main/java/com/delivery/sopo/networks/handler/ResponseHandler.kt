package com.delivery.sopo.networks.handler

import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.google.gson.Gson
import retrofit2.Response

class ResponseHandler<T: Any?>(val response: Response<APIResult<T>>): BaseService()
{
    suspend fun start(): ResponseResult<T?>
    {
        when(val result = apiCall( call = { response }))
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data
                val data = apiResult.data
                return success(data)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                return fail(exception.responseCode, exception.data() as T)
            }
        }
    }

    fun success(data: T?):ResponseResult<T?>{
        return ResponseResult(true, ResponseCode.SUCCESS, data, "")
    }

    fun fail(code: ResponseCode, data: T?):ResponseResult<T?>{
        return when(code.HTTP_STATUS)
        {
            401 ->
            {
                ResponseResult(false, code, data, "Auth Error")
            }
            500 ->
            {
                ResponseResult(false, code, data, "Server Error")
            }
            else ->
            {
                ResponseResult(false, code, data, code.MSG)
            }
        }
    }
}