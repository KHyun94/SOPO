package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.util.SopoLog
import retrofit2.Response
import java.lang.ClassCastException

class NetworkException : Exception
{
    override var message: String = ""
    var httpStatusCode: Int = 500
    var detailCode: ResponseCode = ResponseCode.UNKNOWN_ERROR
    private lateinit var data: Any

    constructor(message: String, httpStatusCode: Int, detailCode: ResponseCode){
        this.message = message
        this.httpStatusCode = httpStatusCode
        this.detailCode = detailCode
    }

    constructor(message: String, httpStatusCode: Int, detailCode: ResponseCode, data: Any){
        this.message = message
        this.httpStatusCode = httpStatusCode
        this.detailCode = detailCode
        this.data = data
    }

    fun<T> getData(): T?
    {
        if(!(::data.isInitialized)) throw Exception("'Exception'의 데이터는 존재하지 않습니다.")

        return try{
            data as T
        }catch(e:ClassCastException)
        {
            SopoLog.e("Type Casting Error", e)
            null
        }catch(e: Exception)
        {
            SopoLog.e("Unknown Error", e)
            null
        }
    }
}