package com.delivery.sopo

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.database.room.RoomActivate
import com.delivery.sopo.di.appModule
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.services.FirebaseService
import com.delivery.sopo.thirdpartyapi.kako.KakaoSDKAdapter
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.kakao.auth.KakaoSDK
import com.kakao.auth.Session
import com.kakao.auth.authorization.accesstoken.AccessToken
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.*

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

        startKoin {
            androidContext(this@SOPOApp)
            modules(appModule)
        }


        //Firebase Init
        FirebaseApp.initializeApp(this)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.setLanguageCode("kr")

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

        SopoLog.d("TESTTEST -> ${userLocalRepository.getTopic()}")

        FirebaseRepository.subscribedToTopic(17, 0).start()
    }



    companion object
    {
        lateinit var INSTANCE: Context
        lateinit var firebaseAuth: FirebaseAuth

        val networkStatus: MutableLiveData<NetworkStatus> by lazy {  MutableLiveData<NetworkStatus>()  }

        var cntOfBeUpdate: MutableLiveData<Int> = MutableLiveData<Int>()
    }
}