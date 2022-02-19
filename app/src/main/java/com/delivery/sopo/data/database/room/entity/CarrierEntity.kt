package com.delivery.sopo.data.database.room.entity

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
        name = "name",
        typeAffinity = ColumnInfo.TEXT
    )
    val name: String,
        @ColumnInfo(
        name = "code",
        typeAffinity = ColumnInfo.TEXT
    )
    val code: String
)