package com.delivery.sopo.services.network_handler

import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import okhttp3.ResponseBody
import retrofit2.Response

abstract class BaseService
{
    protected suspend fun <T: Any> apiCall(call: suspend () -> Response<T>): NetworkResult<T>
    {
        val response: Response<T>

        try
        {
            // api 호출
            response = call.invoke()
        }
        catch (t: Throwable)
        {
            // api 호출 실패 - network error
            return NetworkResult.Error(null, APIException(t = t))
        }

        if (!response.isSuccessful)
        {
            // http status code (200 ~ 300) 이외의 코드
            val errorBody: ResponseBody? = response.errorBody()
            return NetworkResult.Error(response.code(), APIException(response.message(), response.code(), errorBody))
        }

        // http status code 200 ~ 300
        return if (response.body() == null)
        {
            NetworkResult.Error(response.code(), APIException(response.message(), response.code(), null))
        }
        else
        {
            NetworkResult.Success(response.code(), response.body()!!)
        }

    }

    protected fun errorHandler()
    {
        CoroutineExceptionHandler { coroutineContext, throwable ->
            SopoLog.e("${throwable.message}", throwable)
        }
    }
}