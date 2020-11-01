package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SopoJsonPatch(
    @SerializedName("op")
    val op: String = "",
    @SerializedName("path")
    val path: String = "",
    @SerializedName("value")
    val value: String? = null
): Serializable