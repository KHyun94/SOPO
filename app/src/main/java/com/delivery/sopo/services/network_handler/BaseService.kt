package com.delivery.sopo.services.network_handler

import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.models.api.Error
import com.delivery.sopo.models.api.Errors
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Response

abstract class BaseService
{
    protected suspend fun <T: Any> apiCall(call: suspend () -> Response<T>): NetworkResponse<T>
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

        if(!response.isSuccessful)
        {
            convertErrorBody(response.code(), response.errorBody())
        }

        return NetworkResponse(response.code(), response.body())
    }

    private fun convertErrorBody(statusCode: Int, errorBody: ResponseBody?)
    {
        errorBody ?: throw SOPOApiException(statusCode, Error(999, ErrorType.NO_RESOURCE, "'Data'가 존재하지 않음", ""))

        val errorReader = errorBody.charStream()

        val errors = try
        {
            Gson().fromJson(errorReader, Errors::class.java).errors
        }
        catch(e: ClassCastException)
        {
            throw InternalServerException("일시적으로 서비스를 이용할 수 없습니다.", e)
        }

        val error = errors[0]

        SopoLog.e("BaseService Error [status:$statusCode] [res:${error.toString()}]")

        if(statusCode == 500) throw InternalServerException("일시적으로 서비스를 이용할 수 없습니다.", error)

        throw SOPOApiException(statusCode, error)
    }
}