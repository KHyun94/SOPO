package com.delivery.sopo.models.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "PARCEL",
    primaryKeys = arrayOf("REG_DT", "PARCEL_UID")
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
        name = "TRACK_NUM",
        typeAffinity = ColumnInfo.TEXT
    )
    var trackNum: String,
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
        name = "INQUERY_RESULT",
        typeAffinity = ColumnInfo.TEXT
    )
    var inqueryResult: String,
    @ColumnInfo(
        name = "INQUERY_HASH",
        typeAffinity = ColumnInfo.TEXT
    )
    var inqueryHash: String,
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
    var status: Int,
    @ColumnInfo(
        name = "CONFIRM",
        typeAffinity = ColumnInfo.INTEGER
    )
    var confirm: Int
    //TODO: 서버에서 이미 삭제되었는데 confirm에 반영이 안되어있어서 다시 앱에서 서버로 삭제요청을 보낸다면?

)