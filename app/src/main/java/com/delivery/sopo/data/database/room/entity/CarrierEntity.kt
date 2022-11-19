package com.delivery.sopo.data.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.delivery.sopo.R

@Entity(
    tableName = "CARRIER",
    inheritSuperIndices = true
)
data class CarrierEntity(
    @PrimaryKey
    @ColumnInfo(
        name = "code",
        typeAffinity = ColumnInfo.TEXT
    )
    val code: String,
    @ColumnInfo(
        name = "name",
        typeAffinity = ColumnInfo.TEXT
    )
    val name: String,
    @ColumnInfo(
        name = "available",
        typeAffinity = ColumnInfo.INTEGER
    )
    val available: Boolean
)