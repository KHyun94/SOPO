package com.delivery.sopo.models

data class ValidateResult<T>(val result:Boolean, val msg:String, var data:T?, var showType :Int)