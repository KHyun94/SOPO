package com.delivery.sopo

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.map
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.RoomActivate
import com.delivery.sopo.di.appModule
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.thirdpartyapi.kako.KakaoSDKAdapter
import com.delivery.sopo.util.ClipboardUtil
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
import java.util.logging.Handler

class SOPOApp: Application()
{
    val userLocalRepository: UserLocalRepository by inject()
    val parcelStatusRepo: ParcelManagementRepoImpl by inject()
    val parcelRepository: ParcelRepository by inject()
    val OAuthLocalRepository: OAuthLocalRepository by inject()

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
        if(kakaoSDKAdapter == null) kakaoSDKAdapter = KakaoSDKAdapter()

        KakaoSDK.init(kakaoSDKAdapter)

        /** 카카오 토큰 만료시 갱신을 시켜준다**/
        if(Session.getCurrentSession().isOpenable())
        {
            Session.getCurrentSession().checkAndImplicitOpen()
        }
        else
        {
            accessToken = Session.getCurrentSession().tokenInfo
        }

        CoroutineScope(Dispatchers.Default).launch {
            RoomActivate.initializeCarrierInfoIntoDB()
        }

        CoroutineScope(Dispatchers.Main).launch {
            currentPage.postValue(getInitViewPagerNumber())
        }

        CoroutineScope(Dispatchers.Default).launch {
            val oAuthEntity =
                OAuthLocalRepository.get(userLocalRepository.getUserId()) ?: return@launch
            oAuth = OAuthMapper.entityToObject(oAuthEntity)
        }

        SopoLog.d(msg = """
            구독 설정 시간 >>> ${userLocalRepository.getTopic()}
            구독 스코프 >>> ${FirebaseMessaging.INSTANCE_ID_SCOPE}
            구독 isAutoInitEnabled >>> ${FirebaseMessaging.getInstance().isAutoInitEnabled}
        """.trimIndent())

//        android.os.Handler().postDelayed(Runnable { cntOfBeUpdate.postValue(3) }, 5000)
//        android.os.Handler().postDelayed(Runnable { cntOfBeUpdate.postValue(5) }, 10000)
//        android.os.Handler().postDelayed(Runnable { cntOfBeUpdate.postValue(7) }, 25000)
    }

    private suspend fun getInitViewPagerNumber(): Int
    {
        val clipboardText = ClipboardUtil.pasteClipboardText(context = this@SOPOApp)

        if(clipboardText != null) return NavigatorConst.REGISTER_TAB

        val cnt = withContext(Dispatchers.Default) { parcelRepository.getOnGoingDataCnt() }

        return if(cnt == 0)
        {
            NavigatorConst.REGISTER_TAB
        }
        else
        {
            NavigatorConst.INQUIRY_TAB
        }
    }

    companion object
    {
        lateinit var INSTANCE: Context
        lateinit var auth: FirebaseAuth
        lateinit var deviceInfo: String
        lateinit var activity: Activity
        lateinit var alarmManager: AlarmManager

        var currentPage = SingleLiveEvent<Int?>()

        var cntOfBeUpdate: MutableLiveData<Int> = MutableLiveData<Int>()

        var oAuth: OAuthDTO? = null
    }
}