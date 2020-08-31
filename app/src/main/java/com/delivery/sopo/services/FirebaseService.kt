package com.delivery.sopo.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.delivery.sopo.views.SplashView
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception

class FirebaseService: FirebaseMessagingService()
{

    var TAG = "LOG.SOPO.FCM"

    override fun onMessageReceived(remoteMessage: RemoteMessage){
        if (remoteMessage.data.isNotEmpty()){

//            Message m = new Message();
//
//            if(remoteMessage.getData().get("m_no") != null)
//                m.setM_no(Integer.parseInt(remoteMessage.getData().get("m_no")));
//
//            m.setM_sender(remoteMessage.getData().get("m_sender"));
//            m.setM_sender_img(remoteMessage.getData().get("m_sender_img"));
//            m.setM_card_img(remoteMessage.getData().get("m_card_img"));
//            m.setM_msg(remoteMessage.getData().get("m_msg"));
//            m.setM_date(remoteMessage.getData().get("m_date"));
//            m.setM_is_receive(Boolean.parseBoolean(remoteMessage.getData().get("m_is_receive")));
//            m.setM_is_delete(Boolean.parseBoolean(remoteMessage.getData().get("m_is_delete")));
            val intent = Intent("com.example.limky.broadcastreceiver.gogo")
            // intent.putExtra("isPush", true);
//            intent.putExtra("msg", m);
            sendBroadcast(intent)
            Log.d(TAG, "onMessageReceived: " + remoteMessage.data.toString())
        }
        remoteMessage.notification?.let{
            Log.d(TAG, "Notification`s body : " + remoteMessage.notification!!.body)
            Log.d(TAG, "Notification`s body : " + remoteMessage.notification!!.title)
        }
//        sendNotification(remoteMessage);
    }

    private fun sendNotification(remoteMessage: RemoteMessage)
    {
        Log.d(TAG, "sendNotification")
        val intent = Intent(this, SplashView::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val nBuilder = NotificationCompat.Builder(this)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["body"])
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(1000, 1000))
            .setLights(Color.WHITE, 1500, 1500)
            .setContentIntent(contentIntent)
            .setChannelId("notice")
        val nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                remoteMessage.data["channel_id"],
                "channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nManager.createNotificationChannel(channel)
        }
        nManager.notify(0 /* ID of notification */, nBuilder.build())
    }

    override fun onDeletedMessages(){
        super.onDeletedMessages()
    }

    override fun onMessageSent(p0: String){
        super.onMessageSent(p0)
    }

    override fun onSendError(p0: String, p1: Exception){
        super.onSendError(p0, p1)
    }

    override fun onNewToken(s: String){
        super.onNewToken(s)
        Log.d(TAG, "onNewToken: $s")
        sendTokenToServer(s)
    }

    private fun sendTokenToServer(token: String){
        Log.d(TAG, "sendTokenToServer: $token")
    }
}