package com.delivery.sopo.networks.dto

import androidx.annotation.NonNull
import androidx.annotation.Nullable

class Resource<T> private constructor(
    val status: Status,
    val data: T? = null,
    val message: String? = null
)
{
    enum class Status
    {
        SUCCESS, ERROR, LOADING
    }

    companion object
    {
        fun <T> success(@NonNull data: T): Resource<T>
        {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String?, @Nullable data: T): Resource<T>
        {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(@Nullable data: T): Resource<T>
        {
            return Resource(Status.LOADING, data, null)
        }
    }
}