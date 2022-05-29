package com.delivery.sopo

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.data.repositories.local.repository.CarrierRepository
import com.delivery.sopo.data.repositories.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.di.*
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.thirdpartyapi.kako.KakaoSDKAdapter
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.kakao.auth.KakaoSDK
import com.kakao.auth.Session
import com.kakao.auth.authorization.accesstoken.AccessToken
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SOPOApplication: Application()
{
    val parcelStatusRepo: ParcelManagementRepoImpl by inject()
    val parcelRepository: ParcelRepository by inject()
    val carrierRepository: CarrierRepository by inject()

    var kakaoSDKAdapter: KakaoSDKAdapter? = null
    var accessToken: AccessToken? = null

    override fun onCreate()
    {
        super.onCreate()

        INSTANCE = this@SOPOApplication

        startKoin {
            androidContext(this@SOPOApplication)
            modules(listOf(apiModule, serviceModule, viewModelModule, useCaseModule, sourceModule, dbModule))
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
    }

    companion object
    {
        lateinit var INSTANCE: Context
        lateinit var firebaseAuth: FirebaseAuth

        val networkStatus: MutableLiveData<NetworkStatus> by lazy {  MutableLiveData<NetworkStatus>()  }

        var cntOfBeUpdate: MutableLiveData<Int> = MutableLiveData<Int>()
    }
}