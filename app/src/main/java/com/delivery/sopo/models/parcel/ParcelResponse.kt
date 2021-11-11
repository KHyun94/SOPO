package com.delivery.sopo.models.parcel

import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ParcelResponse(
        @SerializedName("parcelId")
    var parcelId: Int,
        @SerializedName("userId")
    var userId: String,
        @SerializedName("waybillNum")
    var waybillNum: String,
        @SerializedName("carrier")
    var carrier: String,
        @SerializedName("alias")
    var alias: String,
        @SerializedName("inquiryResult")
    var inquiryResult: String?,
        @SerializedName("inquiryHash")
    var inquiryHash: String?,
        @SerializedName("deliveryStatus")
    var deliveryStatus: String,
        @SerializedName("regDte")
    var regDte: String,
        @SerializedName("arrivalDte")
    var arrivalDte: String?,
        @SerializedName("auditDte")
    var auditDte: String,
        @SerializedName("status")
    var status: Int?
):Serializable