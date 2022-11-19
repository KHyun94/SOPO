package com.delivery.sopo.presentation.viewmodels.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.data.repositories.parcels.ParcelRepository
import com.delivery.sopo.models.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
        private val parcelRepo: ParcelRepository
                    ):
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
        /*updateTopic()
        updateFCMToken()*/
        viewModelScope.launch(Dispatchers.IO) {
            parcelRepo.updateCarrierInfo()
        }

    }

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    fun postPage(page: Int){
        _currentPage.postValue(page)
    }

 /*   fun hasAppPassword(callback: OnDataCallbackListener<Boolean>) = runBlocking(Dispatchers.Default) {
        val hasAppPassword = appPasswordRepo.get() != null
        callback.invoke(hasAppPassword)
    }

    suspend fun checkIsExistParcels(): Boolean {
        return parcelRepo.getOnGoingDataCnt() > 0
    }

    *//** Update FCM Token  **//*
    private fun updateFCMToken() = scope.launch{
        updateFCMTokenUseCase.invoke()
    }

    private fun updateTopic()
    {
        FirebaseRepository.subscribedTopic()
    }

    suspend fun checkSubscribedTime() = withContext(Dispatchers.Default) {
        SopoLog.i("checkSubscribedTime(...) 호출")

        val ongoingParcelCnt = parcelRepo.getOnGoingDataCnt()

        SopoLog.d("현재 진행 중인 택배 갯수 [data:$ongoingParcelCnt(개)]")

        val subscribedTopic = userDataSource.getTopic()

        SopoLog.d("구독 중인 시간 [data:$subscribedTopic]")

        if(subscribedTopic == "" && ongoingParcelCnt > 0)
        {
            SopoLog.d("구독 중인 시간이 없고, 현재 진행 중인 택배가 있는 상태")
            FirebaseRepository.subscribedToTopicInFCM()
        }
    }*/
}