package com.delivery.sopo.models.api

import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.ErrorType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Error(
        @SerializedName("code") val code: Int,
        @SerializedName("type") val type: ErrorType,
        @SerializedName("message") val message: String,
        @SerializedName("path") var path: String):
        Serializable
{
    companion object
    {
        fun makeError(errorCode: Int): Error
        {
            val errorEnum = enumValues<ErrorEnum>().firstOrNull { it.code == errorCode } ?: ErrorEnum.SYSTEM_ERROR
            return Error(errorCode, errorEnum.errorType, errorEnum.message, "")
        }
    }
}

