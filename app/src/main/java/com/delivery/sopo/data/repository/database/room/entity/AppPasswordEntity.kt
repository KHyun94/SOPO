package com.delivery.sopo.data.repository.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.delivery.sopo.util.TimeUtil

/*
    1. App deviceInfo [PK]
    2. App Password
    3. Audit Time
 */

@Entity(
    tableName = "APP_PASSWORD"
)
data class AppPasswordEntity(
    @PrimaryKey
    @ColumnInfo(
        name = "USER_ID",
        typeAffinity = ColumnInfo.TEXT
    )
    var userId: String,

    @ColumnInfo(
        name = "APP_PASSWORD",
        typeAffinity = ColumnInfo.TEXT
    )
    var appPassword: String,

    @ColumnInfo(
        name = "ALG",
        typeAffinity = ColumnInfo.TEXT
    )
    var alg: String = "SHA256",

    @ColumnInfo(
        name = "AUDIT_DTE",
        typeAffinity = ColumnInfo.TEXT
    )
        var auditDte: String = TimeUtil.getDateTime()
)