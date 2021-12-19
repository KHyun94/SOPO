package com.delivery.sopo.extensions

fun<T> T.wrapBodyAliasToMap(alias:String):Map<String, T> = mapOf<String, T>(Pair(alias, this))
fun<T> T.wrapBodyAliasToHashMap(alias:String):HashMap<String, T> = hashMapOf<String, T>(Pair(alias, this))