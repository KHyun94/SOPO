package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Parcel(
    @SerializedName("parcelId")
    val parcelId: ParcelId,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("trackNum")
    val trackNum: String,
    @SerializedName("carrier")
    val carrier: String,
    @SerializedName("parcelAlias")
    val parcelAlias: String,
    @SerializedName("inqueryResult")
    val inqueryResult: String,
    @SerializedName("inqueryHash")
    val inqueryHash: String,
    @SerializedName("deliveryStatus")
    val deliveryStatus: String,
    @SerializedName("arrivalDte")
    val arrivalDte: String?,
    @SerializedName("auditDte")
    val auditDte: String,
    @SerializedName("status")
    val status: Int?
){
    override fun toString(): String {
        val stringBuffer: StringBuilder = StringBuilder()
        stringBuffer.append("[regDt] : ${parcelId.regDt}    ")
        stringBuffer.append("[parcelUid] : ${parcelId.parcelUid}    ")
        stringBuffer.append("[userName] : $userName ")
        stringBuffer.append("[trackNum] : $trackNum ")
        stringBuffer.append("[carrier] : $carrier   ")
        stringBuffer.append("[parcelAlias] : $parcelAlias   ")
        stringBuffer.append("[inqueryResult] : $inqueryResult   ")
        stringBuffer.append("[inqueryHash] : $inqueryHash   ")
        stringBuffer.append("[deliveryStatus] : $deliveryStatus ")
        stringBuffer.append("[arrivalDte] : $arrivalDte ")
        stringBuffer.append("[status] : $status ")
        return stringBuffer.toString()
    }
}