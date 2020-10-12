package com.delivery.sopo.extensions

import android.util.Log

var isDebug = true

// 로그 extension
fun Log.debugSopo(tag:String?, log:String){
    if(isDebug) Log.d(tag?:"SOPO.LOG.DEBUG", log)
}

fun Log.infoSopo(tag:String?, log:String){
    if(isDebug) Log.d(tag?:"SOPO.LOG.INFO", log)
}


fun Log.errorSopo(tag:String?, log:String){
    if(isDebug) Log.d(tag?:"SOPO.LOG.ERROR", log)
}