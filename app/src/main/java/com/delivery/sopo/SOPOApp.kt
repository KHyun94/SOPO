package com.delivery.sopo

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.di.appModule
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.services.AlarmReceiver
import com.delivery.sopo.thirdpartyapi.kako.KakaoSDKAdapter
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.kakao.auth.KakaoSDK
import com.kakao.auth.Session
import com.kakao.auth.authorization.accesstoken.AccessToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.*

class SOPOApp : Application()
{
    val appDatabase: AppDatabase by inject()
    val parcelRepoImpl: ParcelRepoImpl by inject()

    val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    var kakaoSDKAdapter: KakaoSDKAdapter? = null
    var accessToken: AccessToken? = null


    override fun onCreate()
    {
        super.onCreate()

        INSTANCE = this@SOPOApp

        startKoin {
            androidContext(this@SOPOApp)
            modules(appModule)
        }

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.d(TAG, "${OtherUtil.getDeviceID(SOPOApp.INSTANCE)}")


        //Firebase Init
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("kr")

        //카카오톡 로그인 API 초기화
        if (kakaoSDKAdapter == null)
            kakaoSDKAdapter = KakaoSDKAdapter()

        KakaoSDK.init(kakaoSDKAdapter)

        /** 카카오 토큰 만료시 갱신을 시켜준다**/
        if (Session.getCurrentSession().isOpenable())
        {
            Session.getCurrentSession().checkAndImplicitOpen()
        }
        else
        {
            accessToken = Session.getCurrentSession().tokenInfo
        }

        // FCM TOKEN
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            Log.d(TAG, "토큰 발행: " + task.result!!.token)
        }

        RoomActivate.initCourierDB(this)

        getInitViewPagerNumber(){
            currentPage.postValue(it)
        }

        // todo 푸시 후 처리
//        registerAlarm()
    }

    private fun getInitViewPagerNumber(cb : ((Int) -> Unit))
    {
       ClipboardUtil.pasteClipboardText(con = this, parcelImpl = parcelRepoImpl){
            if (it.isNotEmpty())
           {
               cb.invoke(NavigatorConst.REGISTER_TAB)
           }
           else
           {
               CoroutineScope(Dispatchers.Default).launch {
                   val cnt = parcelRepoImpl.getOnGoingDataCnt()

                   if (cnt == 0)
                   {
                       cb.invoke(NavigatorConst.REGISTER_TAB)
                   }
                   else
                   {
                       cb.invoke(NavigatorConst.INQUIRY_TAB)
                   }
               }
           }
       }
    }

    fun registerAlarm()
    {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, 10)

        Toast.makeText(this, "알람매니저 등록", Toast.LENGTH_LONG).show()
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        when
        {
            Build.VERSION.SDK_INT >= 23 -> alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + cal.timeInMillis, pendingIntent)
            Build.VERSION.SDK_INT >= 19 -> alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.timeInMillis, pendingIntent)
            else -> alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + cal.timeInMillis, pendingIntent)
        }
    }

    companion object
    {
        lateinit var INSTANCE: Context
        lateinit var auth: FirebaseAuth

        lateinit var alarmManager : AlarmManager

        var currentPage = SingleLiveEvent<Int?>()
    }
}