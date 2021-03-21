package com.delivery.sopo.models.push

import com.google.gson.annotations.SerializedName

data class UpdatedParcelInfo(
    @SerializedName("updatedParcelId")
    val updatedParcelId: List<UpdateParcelDao>
)
