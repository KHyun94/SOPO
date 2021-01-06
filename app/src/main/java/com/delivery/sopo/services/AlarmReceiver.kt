package com.delivery.sopo.services

//import androidx.work.OneTimeWorkRequest
//import androidx.work.WorkManager
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
import androidx.core.app.NotificationCompat
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.LogEntity
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import java.text.SimpleDateFormat
import java.util.*


class AlarmReceiver : BroadcastReceiver()
{
    private val appDatabase : AppDatabase by inject(clazz = AppDatabase::class.java)
    private val userRepoImpl : UserRepoImpl by inject(clazz = UserRepoImpl::class.java)
    private val parcelRepoImpl : ParcelRepoImpl by inject(clazz = ParcelRepoImpl::class.java)

    private val TAG = "AlarmReceiver"

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

        cpuWakeLock!!.acquire(10*60*1000L /*10 minutes*/)

//        test(context)
        testNoti(context, "Tile", "Message")
    }

    private fun releaseLock()
    {
        if (wifiLock != null)
        {
            wifiLock!!.release();
            wifiLock = null;
        }

        if (cpuWakeLock != null)
        {
            cpuWakeLock!!.release();
            cpuWakeLock = null;
        }
    }

    fun testNoti(context: Context, msg : String, title : String)
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
        builder.setContentTitle(title) //알림창 제목
        builder.setContentText(msg) //알림창 내용
        //알림창의 큰 이미지

        //건축가에게 알림 객체 생성하도록
        val notification: Notification = builder.build()

        //알림매니저에게 알림(Notify) 요청
        notificationManager!!.notify(1, notification)

        releaseLock()
        //알림 요청시에 사용한 번호를 알림제거 할 수 있음.
        //notificationManager.cancel(1);
    }

    // ===============================================================================================================================================   private val TAG = "SopoWorker"

    // Room 내 등록된 택배(진행 중)의 갯수 > 0 == true != false
    private suspend fun isEnrolledParcel(): Boolean
    {
        val cnt = parcelRepoImpl.getOnGoingDataCnt() ?: 0
        SopoLog.d(tag = "$TAG.isEnrolledParcel", msg = "등록된 소포의 갯수 => $cnt")
        return cnt > 0
    }

    // Room 내 저장된 택배들을 서버로 조회 요청
    private suspend fun patchParcels(): APIResult<String?>?
    {
        // 유저 이메일
        val email = userRepoImpl.getEmail()

        SopoLog.d(tag = "$TAG.patchParcels", msg = "유저 이메일 ===> $email")

        if (isEnrolledParcel())
        {
            SopoLog.d(tag = "$TAG.patchParcels", msg = "is Enrolled Parcel => YES")

            return NetworkManager.privateRetro.create(ParcelAPI::class.java)
                .parcelsRefreshing(email = email)
        }
        else
        {
            // 조회 안함 및 period worker 제거
            SopoLog.d(tag = "$TAG.patchParcels", msg = "is Enrolled Parcel => NO")
            return null
        }
    }


    // worker의 실행 메서드
    fun test(context: Context)
    {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {

                val result = patchParcels()

                if (result != null)
                {
                    SopoLog.d(tag = "$TAG.doWork", msg = "Work Service => $result")

                    if (result.code == "0000")
                    {
                        SopoLog.d(tag = "$TAG.doWork", msg = "Success to build Worker  $result")

                        appDatabase.logDao()
                            .insert(
                                LogEntity(
                                    no = 0,
                                    msg = "Success to PATCH work manager",
                                    uuid = "1",
                                    regDt = TimeUtil.getDateTime()
                                )
                            )

                        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                        testNoti(context = context, msg = "Update Patch 성공!!!", title = "Update Success $time")
                    }
                    else
                    {
                        appDatabase.logDao()
                            .insert(
                                LogEntity(
                                    no = 0,
                                    msg = "Failure to PATCH work manager, Because Of ErrorCode $result",
                                    uuid = "1",
                                    regDt = TimeUtil.getDateTime()
                                )
                            )

                        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                        testNoti(context = context, msg = "Update Patch 실패!!!", title = "Update Fail $time")
                        SopoLog.d(tag = TAG, msg = "Fail to build Worker  $result")
                    }
                }
                else
                {
                    appDatabase.logDao()
                        .insert(
                            LogEntity(
                                no = 0,
                                msg = "Connect Fail to PATCH work manager becuase result null",
                                uuid = "1",
                                regDt = TimeUtil.getDateTime()
                            )
                        )


                    Log.i(TAG, "Work Service Fail")
                    val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                    testNoti(context = context, msg = "Update Patch 실패!!!", title = "Update Fail $time")
                }

            }

            releaseLock()
        }
    }


    companion object
    {
        private var cpuWakeLock: PowerManager.WakeLock? = null
        private var wifiLock: WifiManager.WifiLock? = null
        private var manager: ConnectivityManager? = null
    }
}