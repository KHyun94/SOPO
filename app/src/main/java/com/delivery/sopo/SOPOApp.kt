package com.delivery.sopo

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.LiveData
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.di.appModule
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.thirdpartyapi.kako.KakaoSDKAdapter
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.auth.KakaoSDK
import com.kakao.auth.Session
import com.kakao.auth.authorization.accesstoken.AccessToken
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.*

class SOPOApp : Application()
{
    val appDatabase: AppDatabase by inject()
    val userRepoImpl : UserRepoImpl by inject()
    val parcelRepoImpl: ParcelRepoImpl by inject()
    val oauthRepoImpl : OauthRepoImpl by inject()

    val parcelManagementRepoImpl: ParcelManagementRepoImpl by inject()

    val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    var kakaoSDKAdapter: KakaoSDKAdapter? = null
    var accessToken: AccessToken? = null

    override fun onCreate()
    {
        super.onCreate()

        INSTANCE = this@SOPOApp
        activity = Activity()

        startKoin {
            androidContext(this@SOPOApp)
            modules(appModule)
        }

        deviceInfo = OtherUtil.getDeviceID(INSTANCE)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

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

        RoomActivate.initCourierDB(this)

        getInitViewPagerNumber() {
            currentPage.postValue(it)
        }

        CoroutineScope(Dispatchers.Default).launch {
            oauth = oauthRepoImpl.get(userRepoImpl.getEmail())
        }

        SopoLog.d(msg = """
            구독 설정 시간 >>> ${userRepoImpl.getTopic()}
            구독 스코프 >>> ${FirebaseMessaging.INSTANCE_ID_SCOPE}
            구독 isAutoInitEnabled >>> ${FirebaseMessaging.getInstance().isAutoInitEnabled}
        """.trimIndent())

    }

    private fun getInitViewPagerNumber(cb: ((Int) -> Unit))
    {
        ClipboardUtil.pasteClipboardText(con = this, parcelImpl = parcelRepoImpl) {
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

    companion object
    {
        lateinit var INSTANCE: Context
        lateinit var auth: FirebaseAuth
        lateinit var deviceInfo : String
        lateinit var activity: Activity
        lateinit var alarmManager: AlarmManager

        var currentPage = SingleLiveEvent<Int?>()

        val cntOfBeUpdate: LiveData<Int>
            get() = SOPOApp().parcelManagementRepoImpl.getIsUpdateCntLiveData()

        var oauth : OauthEntity? = null
    }
}