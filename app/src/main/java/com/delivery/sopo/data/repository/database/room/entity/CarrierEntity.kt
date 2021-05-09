package com.delivery.sopo.data.repository.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "CARRIER",
    inheritSuperIndices = true
)
data class CarrierEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(
        name = "carrierNo",
        typeAffinity = ColumnInfo.INTEGER
    )
    val carrierNo: Int = 1,
    @ColumnInfo(
        name = "carrierName",
        typeAffinity = ColumnInfo.TEXT
    )
    val carrierName: String,
    @ColumnInfo(
        name = "carrierCode",
        typeAffinity = ColumnInfo.TEXT
    )
    val carrierCode: String,
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