package com.delivery.sopo.models.parcel

import com.delivery.sopo.database.room.entity.ParcelEntity
import com.google.gson.annotations.SerializedName

data class Parcel(
    @SerializedName("parcelId")
    var parcelId: ParcelId,
    @SerializedName("userName")
    var userName: String,
    @SerializedName("trackNum")
    var trackNum: String,
    @SerializedName("carrier")
    var carrier: String,
    @SerializedName("parcelAlias")
    var parcelAlias: String,
    @SerializedName("inqueryResult")
    var inqueryResult: String,
    @SerializedName("inqueryHash")
    var inqueryHash: String,
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
        this.trackNum = parcelEntity.trackNum
        this.auditDte = parcelEntity.auditDte
        this.arrivalDte = parcelEntity.arrivalDte
        this.carrier = parcelEntity.carrier
        this.deliveryStatus = parcelEntity.deliveryStatus
        this.parcelAlias = parcelEntity.parcelAlias
        this.inqueryHash = parcelEntity.inqueryHash
        this.inqueryResult = parcelEntity.inqueryResult
        this.status = parcelEntity.status
    }

    override fun toString(): String
    {
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