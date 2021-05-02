package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.util.CodeUtil
import com.google.gson.Gson
import retrofit2.Response

class APIException: Exception
{
    var e: Exception = Exception("API Exception")
    var errorMessage: String = "UNKNOWN ERROR"
    var responseCode: ResponseCode = ResponseCode.ERROR_UNKNOWN
    var httpStatusCode: Int = 0

    var apiResult: APIResult<*>? = null

    constructor(e: Exception)
    {
        this.e = e
        this.errorMessage = e.message?:"UNKNOWN ERROR"
    }

    constructor(errorMessage: String, responseCode: ResponseCode, httpStatusCode: Int = 500)
    {
        this.errorMessage = errorMessage
        this.responseCode = responseCode
        this.httpStatusCode = httpStatusCode
        this.e = Exception(errorMessage)
    }

    fun data():Any?
    {
        try {
            return apiResult?.data
        }
        catch (e: Exception) {
            throw e
        }
    }

    companion object{

        var apiResult: APIResult<*>? = null

        fun<T> parse(response: Response<T>): APIException
        {
            val httpStatusCode = response.code()

            val errorReader = response.errorBody()?.charStream()!!
            apiResult = Gson().fromJson(errorReader, APIResult::class.java)

            val responseCode = CodeUtil.getCode(apiResult?.code)
            val errorMessage = apiResult?.message?:"알 수 없는 에러"

            return APIException(errorMessage = errorMessage, responseCode = responseCode, httpStatusCode = httpStatusCode)
        }
    }
}
