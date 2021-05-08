package com.delivery.sopo.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog

@Entity(
    tableName = "PARCEL",
    primaryKeys = ["REG_DT", "PARCEL_UID"]
)
data class ParcelEntity(
    @ColumnInfo(
        name = "REG_DT",
        typeAffinity = ColumnInfo.TEXT
    )
    var regDt: String = "",
    @ColumnInfo(
        name = "PARCEL_UID",
        typeAffinity = ColumnInfo.TEXT
    )
    var parcelUid: String,
    @ColumnInfo(
        name = "USERNAME",
        typeAffinity = ColumnInfo.TEXT
    ) var userName: String,
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
        name = "PARCEL_ALIAS",
        typeAffinity = ColumnInfo.TEXT
    )
    var parcelAlias: String,
    @ColumnInfo(
        name = "INQUIRY_RESULT",
        typeAffinity = ColumnInfo.TEXT
    )
    var inquiryResult: String?,
    @ColumnInfo(
        name = "INQUERY_HASH",
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
    fun update(parcel: Parcel){

        SopoLog.d(msg = "ParcelEntity Update => $parcel")

        this.parcelAlias = parcel.parcelAlias
        this.inquiryResult = parcel.inquiryResult
        this.inquiryHash = parcel.inquiryHash
        this.deliveryStatus = parcel.deliveryStatus
        parcel.arrivalDte?.let {
            this.arrivalDte = it
        }
        this.auditDte = parcel.auditDte
        parcel.status?.let {
            this.status = it
        }
    }
}