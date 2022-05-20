package com.delivery.sopo.models.parcel.tracking_info

import com.delivery.sopo.util.DateUtil
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Progresses (
        @SerializedName("time")
    val time: String?,
        @SerializedName("location")
    val location: Location?,
        @SerializedName("status")
    val status:  Status?,
        @SerializedName("description")
    val description: String?
) : Serializable {
    fun getDate(): Date?
    {
        if(time == null) return null
//        val list = DateUtil.changeDateFormat(time).split(" ")
        val list = DateUtil.changeDateFormat(time, DateUtil.TIMESTAMP_TYPE_AUTH_EXPIRED, DateUtil.DATE_TIME_TYPE_PROGRESSES)?.toString()?.split(" ")?:return null
        return Date(list[0], list[1])
    }
}