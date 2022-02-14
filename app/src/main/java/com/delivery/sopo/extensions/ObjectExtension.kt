package com.delivery.sopo.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun<T> T.wrapBodyAliasToMap(alias:String):Map<String, T> = mapOf<String, T>(Pair(alias, this))
fun<T> T.wrapBodyAliasToHashMap(alias:String):HashMap<String, T> = hashMapOf<String, T>(Pair(alias, this))

inline fun <reified T : Any> T.toJson(gson: Gson = Gson()): String {
    return gson.toJson(this)
}

inline fun <reified T> String.fromJson(gson: Gson = Gson()): T {
    val type = object : TypeToken<T>() {}.type
    return gson.fromJson(this, type)
}