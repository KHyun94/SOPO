package com.delivery.sopo.database.room.dto

import com.delivery.sopo.models.parcel.ParcelId

data class DeleteParcelsDTO(
    var parcelIds: MutableList<ParcelId>? = null
)