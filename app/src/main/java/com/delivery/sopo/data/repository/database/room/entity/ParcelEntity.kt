package com.delivery.sopo.data.repository.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.delivery.sopo.models.parcel.ParcelResponse
import com.delivery.sopo.util.SopoLog

@Entity(
    tableName = "PARCEL",
    primaryKeys = ["PARCEL_ID"]
)
data class ParcelEntity(
    @ColumnInfo(
        name = "PARCEL_ID",
        typeAffinity = ColumnInfo.INTEGER
    )
    var parcelId: Int,
    @ColumnInfo(
        name = "USER_ID",
        typeAffinity = ColumnInfo.TEXT
    ) var userId: String,
    @ColumnInfo(
        name = "WAYBILL_NUM",
        typeAffinity = ColumnInfo.TEXT
    )
    var waybillNum: String,
    @ColumnInfo(
        name = "CARRIER",
        typeAffinity = ColumnInfo.TEXT
    )
    var carrier: String,
    @ColumnInfo(
        name = "ALIAS",
        typeAffinity = ColumnInfo.TEXT
    )
    var alias: String,
    @ColumnInfo(
        name = "INQUIRY_RESULT",
        typeAffinity = ColumnInfo.TEXT
    )
    var inquiryResult: String?,
    @ColumnInfo(
        name = "INQUIRY_HASH",
        typeAffinity = ColumnInfo.TEXT
    )
    var inquiryHash: String?,
    @ColumnInfo(
        name = "DELIVERY_STATUS",
        typeAffinity = ColumnInfo.TEXT
    )
    var deliveryStatus: String,
    @ColumnInfo(
        name = "ARRIVAL_DTE",
        typeAffinity = ColumnInfo.TEXT
    )
    var arrivalDte: String,
    @ColumnInfo(
        name = "REG_DT",
        typeAffinity = ColumnInfo.TEXT
    )
    var regDt: String,
    @ColumnInfo(
        name = "AUDIT_DTE",
        typeAffinity = ColumnInfo.TEXT
    )
    var auditDte: String,
    @ColumnInfo(
        name = "STATUS",
        typeAffinity = ColumnInfo.INTEGER
    )
    var status: Int
){
    fun update(parcelResponse: ParcelResponse){

        SopoLog.d(msg = "ParcelEntity Update => $parcelResponse")

        this.alias = parcelResponse.alias
        this.inquiryResult = parcelResponse.inquiryResult
        this.inquiryHash = parcelResponse.inquiryHash
        this.deliveryStatus = parcelResponse.deliveryStatus
        parcelResponse.arrivalDte?.let {
            this.arrivalDte = it
        }
        this.auditDte = parcelResponse.auditDte
        parcelResponse.status?.let {
            this.status = it
        }
    }
}