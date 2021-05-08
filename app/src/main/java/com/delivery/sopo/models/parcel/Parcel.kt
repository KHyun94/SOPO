package com.delivery.sopo.models.parcel

import com.delivery.sopo.database.room.entity.ParcelEntity
import com.google.gson.annotations.SerializedName

data class Parcel(
    @SerializedName("parcelId")
    var parcelId: ParcelId,
    @SerializedName("userName")
    var userName: String,
    @SerializedName("waybillNum")
    var waybillNum: String,
    @SerializedName("carrier")
    var carrier: String,
    @SerializedName("parcelAlias")
    var parcelAlias: String,
    @SerializedName("inquiryResult")
    var inquiryResult: String?,
    @SerializedName("inquiryHash")
    var inquiryHash: String?,
    @SerializedName("deliveryStatus")
    var deliveryStatus: String,
    @SerializedName("arrivalDte")
    var arrivalDte: String?,
    @SerializedName("auditDte")
    var auditDte: String,
    @SerializedName("status")
    var status: Int?
)
{
    fun update(parcelEntity: ParcelEntity)
    {
        this.parcelId = ParcelId(parcelEntity.parcelUid, parcelEntity.regDt)
        this.userName = parcelEntity.userName
        this.waybillNum = parcelEntity.waybillNum
        this.auditDte = parcelEntity.auditDte
        this.arrivalDte = parcelEntity.arrivalDte
        this.carrier = parcelEntity.carrier
        this.deliveryStatus = parcelEntity.deliveryStatus
        this.parcelAlias = parcelEntity.parcelAlias
        this.inquiryHash = parcelEntity.inquiryHash
        this.inquiryResult = parcelEntity.inquiryResult
        this.status = parcelEntity.status
    }

    override fun toString(): String
    {
        val stringBuffer: StringBuilder = StringBuilder()
        stringBuffer.append("[regDt] : ${parcelId.regDt}    ")
        stringBuffer.append("[parcelUid] : ${parcelId.parcelUid}    ")
        stringBuffer.append("[userName] : $userName ")
        stringBuffer.append("[waybillNum] : $waybillNum ")
        stringBuffer.append("[carrier] : $carrier   ")
        stringBuffer.append("[parcelAlias] : $parcelAlias   ")
        stringBuffer.append("[inquiryResult] : $inquiryResult   ")
        stringBuffer.append("[inquiryHash] : $inquiryHash   ")
        stringBuffer.append("[deliveryStatus] : $deliveryStatus ")
        stringBuffer.append("[arrivalDte] : $arrivalDte ")
        stringBuffer.append("[status] : $status ")
        return stringBuffer.toString()
    }
}