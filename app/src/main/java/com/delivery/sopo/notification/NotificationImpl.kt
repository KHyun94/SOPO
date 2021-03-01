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
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.NotificationEnum
import com.delivery.sopo.interfaces.notification.Notification
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.push.UpdateParcelDao
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.TimeUtil
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

object NotificationImpl: Notification
{

    fun awakenDeviceNoti(remoteMessage: RemoteMessage, context: Context, intent: Intent) {
        val channelId = "${context.packageName}SOPO"

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
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

    override fun alertUpdateParcel(remoteMessage: RemoteMessage, context: Context, intent: Intent, message: String) {
        val channelId = "${context.packageName}SOPO"

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val nBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.app_icon)
            .setContentTitle("SOPO")
            .setContentText(message)
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
        nManager.notify(10001, nBuilder.build())
    }
}