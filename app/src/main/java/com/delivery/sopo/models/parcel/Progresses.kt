package com.delivery.sopo.models.parcel

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
    fun getDate(): Date
    {
        if(time == null) throw Exception("택배 진행사항 중 'time' 오류")
        val list = DateUtil.changeDateFormat(time).split(" ")
        return Date(list[0], list[1])
    }
}