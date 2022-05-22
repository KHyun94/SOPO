package com.delivery.sopo.data.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "AUTH_TOKEN",
    inheritSuperIndices = true
)
data class AuthTokenEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(
        name = "userToken",
        typeAffinity = ColumnInfo.TEXT
    )
    val userToken : String,
    @ColumnInfo(
        name = "accessToken",
        typeAffinity = ColumnInfo.TEXT
    )
    val accessToken : String,

    @ColumnInfo(
        name = "refreshToken",
        typeAffinity = ColumnInfo.TEXT
    )
    val refreshToken : String,
    @ColumnInfo(
        name = "grantType",
        typeAffinity = ColumnInfo.TEXT
    )
    val grantType: String,
    @ColumnInfo(
        name = "expireAt",
        typeAffinity = ColumnInfo.TEXT
    )
    val expireAt: String
)