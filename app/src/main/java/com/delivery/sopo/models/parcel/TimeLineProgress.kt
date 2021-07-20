package com.delivery.sopo.models.parcel

data class TimeLineProgress(
    val date : Date?,
    val location: String?,
    val description : String?,
    val status : Status?
)