package com.delivery.sopo.data.repository.local.o_auth

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "OAUTH",
    inheritSuperIndices = true
)
data class OAuthEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(
        name = "user_id",
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