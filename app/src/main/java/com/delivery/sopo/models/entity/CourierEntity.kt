package com.delivery.sopo.models.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "COURIER",
    inheritSuperIndices = true
)
data class CourierEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(
        name = "courierNo",
        typeAffinity = ColumnInfo.INTEGER
    )
    var courierNo: Int = 1,
    @ColumnInfo(
        name = "courierName",
        typeAffinity = ColumnInfo.TEXT
    )
    var courierName: String,
    @ColumnInfo(
        name = "minLen",
        typeAffinity = ColumnInfo.INTEGER
    ) var minLen: Int,
    @ColumnInfo(
        name = "maxLen",
        typeAffinity = ColumnInfo.INTEGER
    )
    var maxLen: Int,
    @ColumnInfo(
        name = "priority",
        typeAffinity = ColumnInfo.REAL
    )
    var priority: Double? = 1.0,
    @ColumnInfo(
        name = "clickRes",
        typeAffinity = ColumnInfo.INTEGER
    )
    var clickRes: Int? = 0,
    @ColumnInfo(
        name = "nonClickRes",
        typeAffinity = ColumnInfo.INTEGER
    )
    var nonClickRes: Int? = 0,
    @ColumnInfo(
        name = "iconRes",
        typeAffinity = ColumnInfo.INTEGER
    )
    var iconRes: Int? = 0

)