package com.delivery.sopo.data.database.room.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CarrierPattern(
        @SerializedName("code")
        val code: String,
        @SerializedName("length")
        val length: Int,
        @SerializedName("header")
        val header: String
):Serializable