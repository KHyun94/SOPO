package com.delivery.sopo.models.push

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UpdatedParcelInfo(
    @SerializedName("parcelListToPushInfos")
    val updatedParcelIds: List<UpdateParcelDao>
):Serializable
