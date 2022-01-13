package com.delivery.sopo.services

import android.R
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.delivery.sopo.util.SopoLog

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
                SopoLog.d("화이트리스트에 등록되지않음 => package:${context.packageName}")

                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            }
            else
            {
                SopoLog.d("화이트리스트에 등록되어있음")
            }
        }
    }

}