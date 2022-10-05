package com.delivery.sopo.presentation.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.data.repositories.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.enums.SettingEnum
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
        private val userDataSource: UserDataSource,
        private val appPasswordRepo: AppPasswordRepository) : BaseViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
    get() = _navigator

    val isSetOfSecurity: LiveData<Int> = appPasswordRepo.getCntOfAppPasswordLiveData()

    private val _showSetPassword = MutableLiveData<Boolean>()
    val showSetPassword: LiveData<Boolean>
        get() = _showSetPassword

    private val _pushAlarmType = MutableLiveData<SettingEnum.PushAlarmType>()
    val pushAlarmType: LiveData<SettingEnum.PushAlarmType>
        get() = _pushAlarmType

    val notDisturbStartTime = MutableLiveData<String>()
    val notDisturbEndTime = MutableLiveData<String>()

    private val _notDisturbTime = MutableLiveData<String>()
    val notDisturbTime: LiveData<String>
    get() = _notDisturbTime

    init
    {
        viewModelScope.launch {
            val alarmType = SettingEnum.PushAlarmType.valueOf(userDataSource.getPushAlarmType())
            setPushAlarmType(alarmType)

            notDisturbStartTime.postValue(userDataSource.getDisturbStartTime())
            notDisturbEndTime.postValue(userDataSource.getDisturbEndTime())

            if(userDataSource.getDisturbStartTime() != "" && userDataSource.getDisturbEndTime() != "")
            {
                setNotDisturbTime("${userDataSource.getDisturbStartTime()} ~ ${userDataSource.getDisturbEndTime()}" )
            }
            else
            {
                setNotDisturbTime("")
            }
        }
    }

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    fun setNotDisturbStartTime(notDisturbStartTime: String) = viewModelScope.launch{
        this@SettingViewModel.notDisturbStartTime.postValue(notDisturbStartTime)
        userDataSource.setDisturbStartTime(notDisturbStartTime)
    }

    fun setNotDisturbEndTime(notDisturbEndTime: String) = viewModelScope.launch{
        this@SettingViewModel.notDisturbEndTime.postValue(notDisturbEndTime)
        userDataSource.setDisturbEndTime(notDisturbEndTime)
    }

    fun setNotDisturbTime(notDisturbTime: String)
    {
        _notDisturbTime.postValue(notDisturbTime)
    }

    fun setPushAlarmType(pushAlarmType: SettingEnum.PushAlarmType) = viewModelScope.launch{
        _pushAlarmType.postValue(pushAlarmType)
        userDataSource.setPushAlarmType(pushAlarmType)
    }

    fun setAppPassword(){
        if(notDisturbTime.value?.length?:0 > 0)
        {
            setNotDisturbStartTime("")
            setNotDisturbEndTime("")
            setNotDisturbTime("")
        }
        else
        {
            postNavigator(NavigatorConst.TO_NOT_DISTURB)
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
        postNavigator(NavigatorConst.TO_SET_NOTIFY_OPTION)
    }

    fun onSetNotDisturbTime(){
        SopoLog.d("호출")
        postNavigator(NavigatorConst.TO_NOT_DISTURB)
    }

    fun  onSetLockPassword(){
        SopoLog.d("호출")
        postNavigator(NavigatorConst.TO_UPDATE_APP_PASSWORD)
    }

    fun onBackClicked(){
        postNavigator(NavigatorConst.Event.BACK)
    }
}