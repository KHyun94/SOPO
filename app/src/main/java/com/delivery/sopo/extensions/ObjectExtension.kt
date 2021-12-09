package com.delivery.sopo.extensions

fun<T> T.wrapBodyAliasToMap(alias:String):Map<String, T> = mapOf<String, T>(Pair(alias, this))