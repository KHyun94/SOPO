package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.*
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewModel(private val appPasswordRepo: AppPasswordRepository) : BaseViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
    get() = _navigator

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

                return
            }

            _showSetPassword.value = true
        }
    }

    fun onSetPushAlarmListener()
    {
        _navigator.postValue(NavigatorConst.TO_SET_NOTIFY_OPTION)
    }

    fun onSetNotDisturbTime(){
        _navigator.postValue(NavigatorConst.TO_NOT_DISTURB)
    }

    fun  onSetLockPassword(){
        _navigator.postValue(NavigatorConst.TO_UPDATE_APP_PASSWORD)
    }

    override fun onCleared()
    {
        super.onCleared()
        _navigator.postValue("")
    }

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum) { }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}