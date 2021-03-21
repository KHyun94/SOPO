package com.delivery.sopo.networks.dto

import com.delivery.sopo.models.OauthResult
import com.delivery.sopo.models.push.UpdateParcelDao
import com.delivery.sopo.models.push.UpdatedParcelInfo
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class FcmPushDTO(
    @SerializedName("notificationId")
    val notificationId: String,
    @SerializedName("data")
    val data: String
)
{
    fun getUpdateParcel(): UpdatedParcelInfo
    {
        SopoLog.d("getUpdateParcel() call >>> $data")
        return Gson().fromJson(data, UpdatedParcelInfo::class.java)
    }
}