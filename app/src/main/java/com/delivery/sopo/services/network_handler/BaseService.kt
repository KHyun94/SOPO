package com.delivery.sopo.services.network_handler

import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.util.SopoLog
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.NullPointerException

abstract class BaseService
{
    protected suspend fun <T: Any> apiCall(call: suspend () -> Response<T>): NetworkResult<T>
    {
        val response: Response<T>

        try
        {
            // api 호출
            SopoLog.e("Success to call api")
            response = call.invoke()
        }
        catch (e: Exception)
        {
            SopoLog.e("Fail to network working ${e.toString()} ${e.message}")
            // api 호출 실패 - network error
            return NetworkResult.Error(null, APIException(e))
        }

        if (!response.isSuccessful)
        {
            SopoLog.e("Fail to request because of ${response.code()}")
            // http status code (200 ~ 300) 이외의 코드
            return NetworkResult.Error(response.code(), APIException.parse(response))
        }

        // http status code 200 ~ 300
        return if (response.body() == null)
        {
            SopoLog.e("Fail to Body is null}")
            NetworkResult.Error(response.code(), APIException(NullPointerException("Response Body is null")))
        }
        else
        {
            SopoLog.e("Success to network")
            NetworkResult.Success(response.code(), response.body()!!)
        }
    }
}