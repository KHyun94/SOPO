package com.delivery.sopo.viewmodels.main

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.OnDataCallbackListener
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.base.OnActivityResultCallbackListener
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.menus.LockScreenView
import kotlinx.coroutines.*

class MainViewModel(private val userRepo: UserLocalRepository,
                    private val parcelRepo: ParcelRepository,
                    private val appPasswordRepo: AppPasswordRepository):
        BaseViewModel()
{
    // 유효성 및 통신 등의 결과 객체
    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int>
        get() = _currentPage

    init
    {
        updateFCMToken()
        updateTopic()
    }

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    fun postPage(page: Int){
        _currentPage.postValue(page)
    }

    fun hasAppPassword(callback: OnDataCallbackListener<Boolean>) = runBlocking(Dispatchers.Default) {
        val hasAppPassword = appPasswordRepo.get() != null
        callback.invoke(hasAppPassword)
    }

    suspend fun checkIsExistParcels(): Boolean {
        return parcelRepo.getOnGoingDataCnt() > 0
    }

    /** Update FCM Token  **/
    private fun updateFCMToken()
    {
        FirebaseRepository.updateFCMToken()
    }

    private fun updateTopic()
    {
        FirebaseRepository.subscribedTopic()
    }

    suspend fun checkSubscribedTime() = withContext(Dispatchers.Default) {
        SopoLog.i("checkSubscribedTime(...) 호출")

        val ongoingParcelCnt = parcelRepo.getOnGoingDataCnt()

        SopoLog.d("현재 진행 중인 택배 갯수 [data:$ongoingParcelCnt(개)]")

        val subscribedTopic = userRepo.getTopic()

        SopoLog.d("구독 중인 시간 [data:$subscribedTopic]")

        if(subscribedTopic == "" && ongoingParcelCnt > 0)
        {
            SopoLog.d("구독 중인 시간이 없고, 현재 진행 중인 택배가 있는 상태")
            FirebaseRepository.subscribedToTopicInFCM()
        }
    }
}