package com.delivery.sopo.models.parcel

import com.delivery.sopo.models.parcel.tracking_info.Date
import com.delivery.sopo.models.parcel.tracking_info.Status

data class TimeLineProgress(
        val date : Date?,
        val location: String?,
        val description : String?,
        val status : Status?
)