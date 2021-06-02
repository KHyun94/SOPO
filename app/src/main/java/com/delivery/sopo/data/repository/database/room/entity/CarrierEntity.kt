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
        name = "range"
    ) val range: List<Int>,
    @ColumnInfo(
        name = "priority",
        typeAffinity = ColumnInfo.REAL
    )
    val priority: Double? = 1.0
)