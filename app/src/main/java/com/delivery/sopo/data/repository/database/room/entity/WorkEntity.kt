package com.delivery.sopo.data.repository.database.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.delivery.sopo.util.TimeUtil

@Entity(
    tableName = "WORK",
    inheritSuperIndices = true
)
data class WorkEntity(
    @PrimaryKey
    @ColumnInfo(
        name = "WORK_UUID",
        typeAffinity = ColumnInfo.TEXT
    )
    val workUUID: String,
    @ColumnInfo(
        name = "WORK_REG_DT",
        typeAffinity = ColumnInfo.TEXT
    )
    val workRegDt: String = TimeUtil.getDateTime()
)