package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class NotDisturbTimeViewModel: ViewModel(), KoinComponent
{
    private val userLocalRepository: UserLocalRepository by inject()

    val startTime = MutableLiveData<String>()
    val endTime = MutableLiveData<String>()

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
    get() = _navigator

    init
    {
        startTime.value = userLocalRepository.getDisturbStartTime()
        endTime.value = userLocalRepository.getDisturbEndTime()
    }

    fun onFloatNotDisturbTimeDialogClicked(){
        _navigator.postValue(NavigatorConst.TO_FLOATING_DIALOG)
    }

    fun onClearClicked(){
        _navigator.postValue(NavigatorConst.TO_BACK_SCREEN)
    }
}