package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.*
import com.delivery.sopo.repository.local.UserRepo

class SettingViewModel(private val userRepo: UserRepo) : ViewModel() , LifecycleObserver
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    var isSecuritySetting = MutableLiveData<Boolean>()
    var testval = MutableLiveData<Int>()

    init
    {
        testval.value = 0
    }

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

    fun showTestDialog(){
        val value = testval.value ?: 0
        testval.value = value + 1
    }

}