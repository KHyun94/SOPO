package com.delivery.sopo.models.parcel

import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.parcel.tracking_info.TrackingInfo
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Parcel{
        data class Common(
                @SerializedName("parcelId") var parcelId: Int,
                @SerializedName("userId") var userId: Int,
                @SerializedName("waybillNum") var waybillNum: String,
                @SerializedName("carrier") var carrier: String,
                @SerializedName("alias") var alias: String,
//                @SerializedName("inquiryResult") var inquiryResult: String?,
                @SerializedName("inquiryResult") var trackingInfo: TrackingInfo?,
                @SerializedName("inquiryHash") var inquiryHash: String?,
                @SerializedName("deliveryStatus") var deliveryStatus: String,
                @SerializedName("regDte") var regDte: String,
                @SerializedName("arrivalDte") var arrivalDte: String?,
                @SerializedName("auditDte") var auditDte: String,
                @SerializedName("status") var status: Int?): Serializable

        data class Detail(
                val regDt: String,
                val alias: String,
                val carrier: Carrier,
                val waybillNum: String,
                val deliverStatus: String?,
                val timeLineProgresses: MutableList<TimeLineProgress>?
        )
        {
                fun changeRegDtFormat():String
                {
                        val yyMMdd = regDt.split(" ")[0].split("-")
                        return with(yyMMdd) { "${get(0)}년 ${get(1)}월 ${get(2)}일 등록" }
                }
        }

        data class Status(
                var parcelId: Int,
                var isBeDelete: Int = 0,
                var updatableStatus: Int = 0,
                var unidentifiedStatus: Int = 0,
                var deliveredStatus: Int = 0,
                var isNowVisible: Int = 0,
                var auditDte: String = ""
        )

        data class Updatable(
                @SerializedName("parcel")
                val parcel: Parcel.Common,
                @SerializedName("updated")
                val updated: Boolean
        )

        data class Register(
                @SerializedName("waybillNum")
                var waybillNum: String?,
                @SerializedName("carrier")
                var carrier: CarrierEnum?,
                @SerializedName("alias")
                var alias: String?
        ):Serializable
}

