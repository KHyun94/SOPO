package com.delivery.sopo.models.parcel

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
}

