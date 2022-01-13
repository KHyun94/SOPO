package com.delivery.sopo.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.delivery.sopo.util.SopoLog

class SOPONotificationListenerService: NotificationListenerService()
{
    override fun onNotificationPosted(sbn: StatusBarNotification?)
    {
        super.onNotificationPosted(sbn)

        val notification: Notification = sbn?.notification?:return

        SopoLog.d("PackageName :: ${sbn.packageName}")

        if(("com.samsung.android.messaging" != sbn.packageName) || ("com.kakao.talk" != sbn.packageName)) return

        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)

        SopoLog.d("""
            title:$title
            text:$text
            subText:$subText
        """.trimIndent())
    }


    fun readKAKAONotificationListener(){

    }

    fun readMMSNotificationListener(){

    }
}