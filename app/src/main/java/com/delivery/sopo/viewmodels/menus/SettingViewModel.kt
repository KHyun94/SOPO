package com.delivery.sopo.viewmodels.menus

import android.view.View
import androidx.lifecycle.*
import com.delivery.sopo.R
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.user.UserSharedPrefHelper
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.SettingEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewModel(
        private val sharedPref: UserSharedPrefHelper,
        private val appPasswordRepo: AppPasswordRepository) : BaseViewModel()
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


    private val _pushAlarmType = MutableLiveData<SettingEnum.PushAlarmType>()
    val pushAlarmType: LiveData<SettingEnum.PushAlarmType>
        get() = _pushAlarmType

    private val _notDisturbTime = MutableLiveData<String>()
    val notDisturbTime: LiveData<String>
    get() = _notDisturbTime

    init
    {
        setPushAlarmType(sharedPref.getPushAlarmType())

        if(sharedPref.getDisturbStartTime() != "" && sharedPref.getDisturbEndTime() != "")
        {
            setNotDisturbTime("${sharedPref.getDisturbStartTime()} ~ ${sharedPref.getDisturbEndTime()}" )
        }
        else
        {
            setNotDisturbTime("설정 꺼짐")
        }
    }

    fun setNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    fun setNotDisturbTime(notDisturbTime: String)
    {
        _notDisturbTime.postValue(notDisturbTime)
    }

    fun setPushAlarmType(pushAlarmType: SettingEnum.PushAlarmType)
    {
        _pushAlarmType.postValue(pushAlarmType)
        sharedPref.setPushAlarmType(pushAlarmType)
    }

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

    fun deleteAppPassword(){
        viewModelScope.launch(Dispatchers.IO){
            appPasswordRepo.deleteAll()
        }
    }

    fun onSetPushAlarmListener()
    {
        SopoLog.d("호출")
        _navigator.postValue(NavigatorConst.TO_SET_NOTIFY_OPTION)
    }

    fun onSetNotDisturbTime(){
        SopoLog.d("호출")
        _navigator.postValue(NavigatorConst.TO_NOT_DISTURB)
    }

    fun  onSetLockPassword(){
        SopoLog.d("호출")
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