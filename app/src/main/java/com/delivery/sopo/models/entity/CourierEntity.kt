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
    val courierNo: Int = 1,
    @ColumnInfo(
        name = "courierName",
        typeAffinity = ColumnInfo.TEXT
    )
    val courierName: String,
    @ColumnInfo(
        name = "courierCode",
        typeAffinity = ColumnInfo.TEXT
    )
    val courierCode: String,
    @ColumnInfo(
        name = "minLen",
        typeAffinity = ColumnInfo.INTEGER
    ) val minLen: Int,
    @ColumnInfo(
        name = "maxLen",
        typeAffinity = ColumnInfo.INTEGER
    )
    val maxLen: Int,
    @ColumnInfo(
        name = "priority",
        typeAffinity = ColumnInfo.REAL
    )
    val priority: Double? = 1.0,
    @ColumnInfo(
        name = "clickRes",
        typeAffinity = ColumnInfo.INTEGER
    )
    val clickRes: Int? = 0,
    @ColumnInfo(
        name = "nonClickRes",
        typeAffinity = ColumnInfo.INTEGER
    )
    val nonClickRes: Int? = 0,
    @ColumnInfo(
        name = "iconRes",
        typeAffinity = ColumnInfo.INTEGER
    )
    val iconRes: Int? = 0

)