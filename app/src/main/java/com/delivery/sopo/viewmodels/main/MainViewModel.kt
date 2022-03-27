package com.delivery.sopo.viewmodels.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class MainViewModel(private val userRepo: UserLocalRepository,
                    private val parcelRepo: ParcelRepository):
        BaseViewModel()
{
    // 유효성 및 통신 등의 결과 객체
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    override val exceptionHandler: CoroutineExceptionHandler
        get() = CoroutineExceptionHandler { coroutineContext, throwable ->  }

    init
    {
        updateFCMToken()
    }

    suspend fun checkIsExistParcels(): Boolean {
        return parcelRepo.getOnGoingDataCnt() > 0
    }

    /** Update FCM Token  **/
    private fun updateFCMToken()
    {
        FirebaseRepository.updateFCMToken()
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