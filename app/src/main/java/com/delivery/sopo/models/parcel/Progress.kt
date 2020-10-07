package com.delivery.sopo.models.parcel

data class Progress(
    val date : Date?,
    val location: String?,
    val description : String?,
    val status : Status?
)