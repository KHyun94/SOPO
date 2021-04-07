package com.delivery.sopo.networks.handler

import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import retrofit2.Response


class ResponseHandler<T: Any?>(val response: Response<APIResult<T>>): BaseService()
{
    suspend fun mainThread(): ResponseResult<T>
    {
        when(val result = apiCall( call = { response }))
        {
            is NetworkResult.Success ->
            {
//                success()
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val responseCode = exception.responseCode
            }
        }
    }

    fun success(data: T):ResponseResult<T>{
        return ResponseResult(ResponseCode.SUCCESS, data, "")
    }

    fun fail(){

    }
}