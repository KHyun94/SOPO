package com.delivery.sopo.services.network_handler

import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import retrofit2.Response

abstract class BaseServiceBeta
{
    protected suspend fun <T: Any> apiCall(call: suspend () -> Response<T>): NetworkResponseBeta<T>
    {
        val response: Response<T>

        try
        {
            response = call.invoke()
        }
        catch(e: Exception)
        {
            throw InternalServerException("일시적으로 서비스를 이용할 수 없습니다.", e)
        }

        // http status code (400 ~ 500)
        if(!response.isSuccessful) return convertErrorBody(response.code(), response.errorBody())

        // response body is null
        if(response.body() == null) return checkBodyNotNull(response.code())

        return NetworkResponseBeta.Success(response.code(), response.body()!!)
    }

    private fun checkBodyNotNull(statusCode: Int): NetworkResponseBeta<Nothing>
    {
        return when(statusCode)
        {
            in 200..399 ->
            {
                val errorResponse = ErrorResponse(999, ErrorType.NO_RESOURCE, "API 성공했지만, response Body가 null", "")
                NetworkResponseBeta.Error(statusCode = statusCode, errorResponse = errorResponse)
            }
            else ->
            {
                NetworkResponseBeta.SuccessNoBody(statusCode = statusCode)
            }
        }
    }

    private fun convertErrorBody(statusCode: Int, errorBody: okhttp3.ResponseBody?): NetworkResponseBeta.Error
    {
        errorBody ?: return NetworkResponseBeta.Error(statusCode, ErrorResponse(999, ErrorType.NO_RESOURCE, "'Data'가 존재하지 않음", ""))
        val errorReader = errorBody.charStream()
        val errorResponse = Gson().fromJson(errorReader, ErrorResponse::class.java)

        if(statusCode == 500)
        {
            throw InternalServerException("일시적으로 서비스를 이용할 수 없습니다.", errorResponse)
        }

        return NetworkResponseBeta.Error(statusCode, errorResponse)
    }
}