package com.delivery.sopo.data.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.delivery.sopo.util.TimeUtil

@Entity(
    tableName = "PARCEL_STATUS",
    primaryKeys = ["PARCEL_ID"]
)
data class ParcelStatusEntity(
    @ColumnInfo(
        name = "PARCEL_ID",
        typeAffinity = ColumnInfo.INTEGER
    )
    var parcelId: Int,
    @ColumnInfo(
        name = "isBeDelete",
        typeAffinity = ColumnInfo.INTEGER
    )
    var isBeDelete: Int = 0,
    // 신규 택배 정보가 도착해서 업데이트 하기 전 업데이트 가능한 상태
    @ColumnInfo(
        name = "updatableStatus",
        typeAffinity = ColumnInfo.INTEGER
    )
    var updatableStatus: Int = 0,
    // 신규 택배 데이터가 내부 디비 상 업데이트 된 후 조회 리스트에서 클릭 또는 새로고침을 통해 확인되지 않은 상태
    @ColumnInfo(
        name = "unidentifiedStatus",
        typeAffinity = ColumnInfo.INTEGER
    )
    var unidentifiedStatus: Int = 0,
    @ColumnInfo(
        name = "deliveredStatus",
        typeAffinity = ColumnInfo.INTEGER
    )
    var deliveredStatus: Int = 0,
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