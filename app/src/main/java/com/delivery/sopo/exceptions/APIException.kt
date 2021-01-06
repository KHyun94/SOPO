package com.delivery.sopo.exceptions

import android.util.Log
import com.delivery.sopo.models.api.APIResult
import com.google.gson.Gson
import okhttp3.ResponseBody

class APIException : Exception
{
    var responseMessage: String? = null
    var responseCode: Int? = null
    var errorBody: ResponseBody? = null
    var t: Throwable? = null

    constructor(t: Throwable)
    {
        this.responseMessage = t.message
        this.t = t
    }

    constructor(responseMessage: String, responseCode: Int, errorBody: ResponseBody?)
    {
        this.responseMessage = responseMessage
        this.responseCode = responseCode
        this.errorBody = errorBody
    }

    constructor(responseMessage: String, responseCode: Int, errorBody: ResponseBody?, t: Throwable)
    {
        this.responseMessage = responseMessage
        this.responseCode = responseCode
        this.errorBody = errorBody
        this.t = t
    }

    fun data(): APIResult<*>?
    {
        val errorReader = errorBody!!.charStream()

        return Gson().fromJson(errorReader, APIResult::class.java)
    }

}
