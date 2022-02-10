package com.delivery.sopo.data.database.room.dto

import com.google.gson.annotations.SerializedName

data class AppPasswordDTO(
        @SerializedName("userId")
        val userId:String,
        @SerializedName("appPassword")
        val appPassword: String,
        @SerializedName("auditDte") var auditDte: String
)