package com.delivery.sopo.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "OAUTH",
    inheritSuperIndices = true
)
data class OauthEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(
        name = "email",
        typeAffinity = ColumnInfo.TEXT
    )
    val email : String,
    @ColumnInfo(
        name = "accessToken",
        typeAffinity = ColumnInfo.TEXT
    )
    val accessToken : String,
    @ColumnInfo(
        name = "tokenType",
        typeAffinity = ColumnInfo.TEXT
    )
    val tokenType: String,
    @ColumnInfo(
        name = "refreshToken",
        typeAffinity = ColumnInfo.TEXT
    )
    val refreshToken: String,
    @ColumnInfo(
        name = "expiresIn",
        typeAffinity = ColumnInfo.TEXT
    )
    val expiresIn: String,
    @ColumnInfo(
        name = "scope",
        typeAffinity = ColumnInfo.TEXT
    )
    val scope: String,
    @ColumnInfo(
        name = "refresh_token_expire_at",
        typeAffinity = ColumnInfo.TEXT
    )
    val refreshTokenExpiredAt: String
)