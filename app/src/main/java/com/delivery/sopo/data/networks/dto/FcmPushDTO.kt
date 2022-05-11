package com.delivery.sopo.data.networks.dto

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class FcmPushDTO(
        @SerializedName("notificationId") val notificationId: String,
        @SerializedName("data") val data: String?)
{
    fun getUpdateParcel(): List<Int>
    {
        return Gson().fromJson(data, object: TypeToken<List<Int>>() {}.type)
    }
}