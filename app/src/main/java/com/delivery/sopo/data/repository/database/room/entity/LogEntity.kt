package com.delivery.sopo.data.repository.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "LOG",
    inheritSuperIndices = true
)
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(
        name = "no",
        typeAffinity = ColumnInfo.INTEGER
    )
    val no: Int,
    @ColumnInfo(
        name = "msg",
        typeAffinity = ColumnInfo.TEXT
    )
    val msg: String,
    @ColumnInfo(
        name = "uuid",
        typeAffinity = ColumnInfo.TEXT
    )
    val uuid: String,
    @ColumnInfo(
        name = "regDt",
        typeAffinity = ColumnInfo.TEXT
    )
    val regDt: String
)