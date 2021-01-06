package com.delivery.sopo.services.workmanager

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.util.SopoLog

class OneTimeWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(
    context,
    params
)
{
    val TAG = "LOG.SOPO"

    override suspend fun doWork(): Result
    {
        SopoLog.d(
            tag = TAG,
            msg = "Period Time Worker Service Start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        )

//        CoroutineScope(Dispatchers.Main).launch {
//            withContext(Dispatchers.Main)
//            {
//                Toast.makeText(
//                    context,
//                    "Period Time Worker Service Start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",
//                    Toast.LENGTH_LONG
//                ).show()
//
//            }
//        }

        return Result.success()
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
}