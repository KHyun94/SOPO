package com.delivery.sopo.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

object PowerManager
{
    fun checkWhiteList(context : Context)
    {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        var isWhiteList  =  false

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            isWhiteList = powerManager.isIgnoringBatteryOptimizations(context.packageName)

            if(!isWhiteList)
            {
                Log.d("LOG.SOPO", "화이트리스트에 등록되지않음 => package:${context.packageName}")

                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:${context.packageName}")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            else
            {
                Log.d("LOG.SOPO", "화이트리스트에 등록되어있음")
            }
        }
    }
}