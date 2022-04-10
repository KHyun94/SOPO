package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.SettingEnum
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewModel(
        private val userLocalRepo: UserLocalRepository,
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
        setPushAlarmType(userLocalRepo.getPushAlarmType())

        notDisturbStartTime.postValue(userLocalRepo.getDisturbStartTime())
        notDisturbEndTime.postValue(userLocalRepo.getDisturbEndTime())

        if(userLocalRepo.getDisturbStartTime() != "" && userLocalRepo.getDisturbEndTime() != "")
        {
            setNotDisturbTime("${userLocalRepo.getDisturbStartTime()} ~ ${userLocalRepo.getDisturbEndTime()}" )
        }
        else
        {
            setNotDisturbTime("")
        }
    }

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    fun setNotDisturbStartTime(notDisturbStartTime: String)
    {
        this.notDisturbStartTime.postValue(notDisturbStartTime)
        userLocalRepo.setDisturbStartTime(notDisturbStartTime)
    }

    fun setNotDisturbEndTime(notDisturbEndTime: String)
    {
        this.notDisturbEndTime.postValue(notDisturbEndTime)
        userLocalRepo.setDisturbEndTime(notDisturbEndTime)
    }

    fun setNotDisturbTime(notDisturbTime: String)
    {
        _notDisturbTime.postValue(notDisturbTime)
    }

    fun setPushAlarmType(pushAlarmType: SettingEnum.PushAlarmType)
    {
        _pushAlarmType.postValue(pushAlarmType)
        userLocalRepo.setPushAlarmType(pushAlarmType)
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

//    override fun onCleared()
//    {
//        super.onCleared()
//        postNavigator("")
//    }


    fun onBackClicked(){
        postNavigator(NavigatorConst.TO_BACK_SCREEN)
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum) { }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}