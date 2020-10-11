package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.*
import com.delivery.sopo.repository.impl.AppPasswordRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewModel(
    private val userRepoImpl: UserRepoImpl,
    private val appPasswordRepo: AppPasswordRepoImpl) : ViewModel() , LifecycleObserver
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    private val _isSetOfSecurity = appPasswordRepo.getCntOfAppPasswordLiveData()
    val isSetOfSecurity: LiveData<Int>
        get() = _isSetOfSecurity

    private val _showSetPassword = MutableLiveData<Boolean>()
    val showSetPassword: LiveData<Boolean>
        get() = _showSetPassword

    fun setAppPassword(){
        _isSetOfSecurity.value?.also {
            if(it>0){
                viewModelScope.launch(Dispatchers.IO){
                    appPasswordRepo.deleteAll()
                }
            }
            else{
                _showSetPassword.value = true
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifeCycleResume(){
        /*
         *  내부 저장소로부터 값을 읽어와 설정여부를 판단한다.
         *  userRepo.getAppPassword() 값이 빈 값 O ==> 설정 안 함
         *  userRepo.getAppPassword() 값이 빈 값 X ==> 설정함
         */
//        isSecuritySetting.value = userRepo.getAppPassword() != ""
    }
}