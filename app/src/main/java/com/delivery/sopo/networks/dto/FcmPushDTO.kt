package com.delivery.sopo.networks.dto

import com.delivery.sopo.models.push.UpdateParcelDao
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class FcmPushDTO(
    @SerializedName("notificationId")
    val notificationId: String,
    @SerializedName("data")
    val data: String
)
{
    fun setUpdateParcel() = Gson().fromJson(data, UpdateParcelDao::class.java)


}