package com.delivery.sopo.models.parcel

import com.delivery.sopo.models.Carrier
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Parcel{
        data class Common(
                @SerializedName("parcelId") var parcelId: Int,
                /**
                 * 삭제되도 됨
                 */
                @SerializedName("userId") var userId: Int,
                @SerializedName("waybillNum") var waybillNum: String,
                @SerializedName("carrier") var carrier: String,
                @SerializedName("alias") var alias: String,
                @SerializedName("inquiryResult") var inquiryResult: String?,
                @SerializedName("inquiryHash") var inquiryHash: String?,
                @SerializedName("deliveryStatus") var deliveryStatus: String,
                @SerializedName("regDte") var regDte: String,
                @SerializedName("arrivalDte") var arrivalDte: String?,
                @SerializedName("auditDte") var auditDte: String,
                @SerializedName("status") var status: Int?): Serializable

        data class Detail(
                // 앱에서 택배 등록한 일자
                val regDt: String,
                // 택배 별칭 "Default:default" -> if default {from_name}이 보내신 택배
                val alias: String,
                // 택배사
                val carrier: Carrier,
                // 운송장 번호
                val waybillNum: String,
                // 택배 상세 정보
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
                val parcelResponse: Parcel.Common,
                @SerializedName("updated")
                val updated: Boolean
        )

}

