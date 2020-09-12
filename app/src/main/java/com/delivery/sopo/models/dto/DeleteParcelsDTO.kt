package com.delivery.sopo.models.dto

import com.delivery.sopo.models.parcel.ParcelId

data class DeleteParcelsDTO(
    var parcelIds: MutableList<ParcelId>? = null
)