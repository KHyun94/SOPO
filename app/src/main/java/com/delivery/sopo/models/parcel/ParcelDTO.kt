package com.delivery.sopo.models.parcel

import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.google.gson.annotations.SerializedName

data class ParcelDTO(
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
        this.parcelId = parcelEntity.parcelId
        this.userId = parcelEntity.userId
        this.waybillNum = parcelEntity.waybillNum
        this.auditDte = parcelEntity.auditDte
        this.arrivalDte = parcelEntity.arrivalDte
        this.carrier = parcelEntity.carrier
        this.deliveryStatus = parcelEntity.deliveryStatus
        this.alias = parcelEntity.alias
        this.inquiryHash = parcelEntity.inquiryHash
        this.inquiryResult = parcelEntity.inquiryResult
        this.status = parcelEntity.status
    }

    override fun toString(): String
    {
        val stringBuffer: StringBuilder = StringBuilder()
        stringBuffer.append("[parcelId] : $parcelId    ")
        stringBuffer.append("[userId] : $userId ")
        stringBuffer.append("[waybillNum] : $waybillNum ")
        stringBuffer.append("[carrier] : $carrier   ")
        stringBuffer.append("[parcelAlias] : $alias   ")
        stringBuffer.append("[inquiryResult] : $inquiryResult   ")
        stringBuffer.append("[inquiryHash] : $inquiryHash   ")
        stringBuffer.append("[deliveryStatus] : $deliveryStatus ")
        stringBuffer.append("[arrivalDte] : $arrivalDte ")
        stringBuffer.append("[status] : $status ")
        return stringBuffer.toString()
    }
}