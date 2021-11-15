package com.delivery.sopo.models.parcel

import androidx.room.ColumnInfo
import com.delivery.sopo.util.TimeUtil

data class ParcelStatus(
        var parcelId: Int,
        var isBeDelete: Int = 0,
        var updatableStatus: Int = 0,
        var unidentifiedStatus: Int = 0,
        var deliveredStatus: Int = 0,
        var isNowVisible: Int = 0,
        var auditDte: String = ""
)