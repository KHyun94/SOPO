package com.delivery.sopo

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.di.appModule
import com.delivery.sopo.services.SOPOWorker
import com.delivery.sopo.thirdpartyapi.kako.KakaoSDKAdapter
import com.delivery.sopo.util.OtherUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.kakao.auth.KakaoSDK
import com.kakao.auth.Session
import com.kakao.auth.authorization.accesstoken.AccessToken
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class SOPOApp : Application()
{

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

       checkWorkManager()
    }

    private fun checkWorkManager()
    {
        val workConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val workRequest = PeriodicWorkRequestBuilder<SOPOWorker>(15, TimeUnit.MINUTES)
            .setConstraints(workConstraint).build()


        val workerManager = WorkManager.getInstance(this)

        val workerState = workerManager.getWorkInfoByIdLiveData(workRequest.id)

        workerManager.enqueue(workRequest)

//        val statusLiveData = workerManager
    }

    companion object
    {
        lateinit var INSTANCE: Context
        lateinit var auth: FirebaseAuth
    }
}