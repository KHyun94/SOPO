package com.delivery.sopo.data.repository.database.room.dto

import com.delivery.sopo.models.parcel.ParcelId

// todo Serialize 처리
data class DeleteParcelsDTO(
    var parcelIds: MutableList<ParcelId>? = null
)