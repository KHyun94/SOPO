package com.delivery.sopo.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.delivery.sopo.util.TimeUtil

@Entity(
    tableName = "PARCEL_MANAGEMENT",
    primaryKeys = ["REG_DT", "PARCEL_UID"]
)
data class ParcelManagementEntity(
    @ColumnInfo(
        name = "REG_DT",
        typeAffinity = ColumnInfo.TEXT
    )
    var regDt: String,
    @ColumnInfo(
        name = "PARCEL_UID",
        typeAffinity = ColumnInfo.TEXT
    )
    var parcelUid: String,
    @ColumnInfo(
        name = "isBeDelete",
        typeAffinity = ColumnInfo.INTEGER
    )
    var isBeDelete: Int = 0,
    @ColumnInfo(
        name = "isBeUpdate",
        typeAffinity = ColumnInfo.INTEGER
    )
    var isBeUpdate: Int = 0,
    @ColumnInfo(
        name = "isUnidentified",
        typeAffinity = ColumnInfo.INTEGER
    )
    var isUnidentified: Int = 0,
    @ColumnInfo(
        name = "isBeDelivered",
        typeAffinity = ColumnInfo.INTEGER
    )
    var isBeDelivered: Int = 0,
    @ColumnInfo(
        name = "isNowVisible",
        typeAffinity = ColumnInfo.INTEGER
    )
    var isNowVisible: Int = 0,
    @ColumnInfo(
        name = "AUDIT_DTE",
        typeAffinity = ColumnInfo.TEXT
    )
    var auditDte: String = TimeUtil.getDateTime()
)