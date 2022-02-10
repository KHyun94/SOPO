package com.delivery.sopo.data.database.room.util

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters
{
    @TypeConverter
    fun listToJson(value: List<Int>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Int>::class.java).toList()
}