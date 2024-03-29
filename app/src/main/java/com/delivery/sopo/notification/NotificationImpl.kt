package com.delivery.sopo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.delivery.sopo.R
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.interfaces.notification.Notification
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.TimeUtil
import com.delivery.sopo.views.splash.SplashView
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

object NotificationImpl: Notification
{

    fun awakenDeviceNoti(remoteMessage: RemoteMessage, context: Context, intent: Intent) {
        val channelId = "${context.packageName}SOPO"

        val intent = Intent(context, SplashView::class.java)
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("test", "팡효!!!")

//        val pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val nBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_icon_notification)
            .setContentTitle("SOPO")
            .setContentText("디바이스 어웨이큰 상태가 변경되었습니다. ${TimeUtil.getDateTime()}")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(1000, 1000))
            .setLights(Color.WHITE, 1500, 1500)
            .setContentIntent(contentIntent)
        val nManager = context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                channelId,
                NotificationEnum.PUSH_UPDATE_PARCEL.channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nManager.createNotificationChannel(channel)
        }
        nManager.notify(30001, nBuilder.build())
    }

    override fun alertUpdateParcel(remoteMessage: RemoteMessage, context: Context, intent: Intent, vararg message: String) {
        val channelId = "${context.packageName}SOPO"

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val nBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.app_icon)
            .setContentTitle("SOPO ${message[0]}")
            .setAutoCancel(true)
            .setStyle(NotificationCompat.InboxStyle().also { style ->
                message.forEach { style.addLine(it) }
            })
            .setColor(context.resources.getColor(R.color.COLOR_MAIN_BLUE_50))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(1000, 1000))
            .setLights(Color.WHITE, 1500, 1500)
            .setContentIntent(contentIntent)

        val nManager = context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                channelId,
                "업데이트 확인 여부",
                NotificationManager.IMPORTANCE_HIGH
            )
            nManager.createNotificationChannel(channel)
        }
        nManager.notify(OtherUtil.getRandomInteger(5), nBuilder.build())
    }
}