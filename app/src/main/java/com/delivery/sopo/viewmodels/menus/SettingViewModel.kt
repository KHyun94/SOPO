package com.delivery.sopo.viewmodels.menus

import android.util.Log
import androidx.lifecycle.*
import com.delivery.sopo.repository.UserRepo

class SettingViewModel(private val userRepo: UserRepo) : ViewModel() , LifecycleObserver
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    var isSecuritySetting = MutableLiveData<Boolean>()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifeCycleResume(){
        /*
         *  내부 저장소로부터 값을 읽어와 설정여부를 판단한다.
         *  userRepo.getAppPassword() 값이 빈 값 O ==> 설정 안 함
         *  userRepo.getAppPassword() 값이 빈 값 X ==> 설정함
         */
        isSecuritySetting.value = userRepo.getAppPassword() != ""
    }

    fun toggleSecuritySetting(){
        val isSecurity = isSecuritySetting.value

        isSecurity?.let {
            isSecuritySetting.value = !isSecurity
        }
    }
}