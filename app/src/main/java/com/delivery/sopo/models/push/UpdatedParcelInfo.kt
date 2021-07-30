package com.delivery.sopo.models.push

import com.google.gson.annotations.SerializedName

data class UpdatedParcelInfo(
    @SerializedName("parcelListToPushInfos")
    val updatedParcelIds: List<UpdateParcelDao>
)
