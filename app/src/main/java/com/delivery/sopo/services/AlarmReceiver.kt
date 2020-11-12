package com.delivery.sopo.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.delivery.sopo.services.workmanager.OneTimeWorker
import com.delivery.sopo.services.workmanager.SOPOWorkeManager
import java.util.concurrent.TimeUnit


class AlarmReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (cpuWakeLock != null) return
        if (wifiLock != null) return

        val wifiManager: WifiManager = context?.getSystemService(WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock("wifilock")
        wifiLock.let {
            it!!.setReferenceCounted(true)
            it.acquire()
        }

        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager

        cpuWakeLock = powerManager.newWakeLock(
            (PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE or PowerManager.PARTIAL_WAKE_LOCK),
            "app:alarm"
        )

        cpuWakeLock!!.acquire()

        Toast.makeText(context, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", Toast.LENGTH_LONG).show()
        testNoti(context = context)

        if(wifiLock != null) {
            wifiLock!!.release();
            wifiLock = null;
        }

        if (cpuWakeLock != null) {
            cpuWakeLock!!.release();
            cpuWakeLock = null;
        }
    }

    private fun testNoti(context: Context)
    {
        //알림(Notification)을 관리하는 관리자 객체를 운영체제(Context)로부터 소환하기
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        //Notification 객체를 생성해주는 건축가객체 생성(AlertDialog 와 비슷)
        var builder: NotificationCompat.Builder? = null

        //Oreo 버전(API26 버전)이상에서는 알림시에 NotificationChannel 이라는 개념이 필수 구성요소가 됨.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channelID = "channel_01" //알림채널 식별자
            val channelName = "MyChannel01" //알림채널의 이름(별명)

            //알림채널 객체 만들기
            val channel =
                NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT)

            //알림매니저에게 채널 객체의 생성을 요청
            notificationManager!!.createNotificationChannel(channel)

            //알림건축가 객체 생성
            builder = NotificationCompat.Builder(context, channelID)
        }
        else
        {
            //알림 건축가 객체 생성
            builder = NotificationCompat.Builder(context)
        }

        //건축가에게 원하는 알림의 설정작업
        builder.setSmallIcon(R.drawable.ic_menu_view)

        //상태바를 드래그하여 아래로 내리면 보이는
        //알림창(확장 상태바)의 설정
        builder.setContentTitle("Title") //알림창 제목
        builder.setContentText("Messages....") //알림창 내용
        //알림창의 큰 이미지

        //건축가에게 알림 객체 생성하도록
        val notification: Notification = builder.build()

        //알림매니저에게 알림(Notify) 요청
        notificationManager!!.notify(1, notification)

        //알림 요청시에 사용한 번호를 알림제거 할 수 있음.
        //notificationManager.cancel(1);
    }

    companion object
    {
        private var cpuWakeLock: PowerManager.WakeLock? = null
        private var wifiLock: WifiManager.WifiLock? = null
        private var manager: ConnectivityManager? = null
    }
}