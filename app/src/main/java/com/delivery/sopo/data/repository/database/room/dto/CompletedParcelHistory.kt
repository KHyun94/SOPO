package com.delivery.sopo.data.repository.database.room.dto

import com.delivery.sopo.util.TimeUtil
import com.google.gson.annotations.SerializedName

data class CompletedParcelHistory(
        @SerializedName("time")
        val date: String,
        @SerializedName("count")
        var count: Int,
        var visibility: Int = 0,
        var status: Int = 1,
        var auditDte: String = TimeUtil.getDateTime()
){
    var year: String = ""
    var month: String = ""

    init
    {
        parseYear()
        parseMonth()
    }

    fun parseYear():String
    {
        val dates = this.date.split('-')
        this.year = dates[0]
        return this.year
    }

    fun parseMonth():String
    {
        val dates = this.date.split('-')
        this.month = dates[1]
        return this.month
    }
}

