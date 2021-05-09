package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class NotDisturbTimeViewModel: ViewModel(), KoinComponent
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    private val userLocalRepository: UserLocalRepository by inject()

    val startTime = MutableLiveData<String>()
    val endTime = MutableLiveData<String>()

    init
    {
        startTime.value = userLocalRepository.getDisturbStartTime()
        endTime.value = userLocalRepository.getDisturbEndTime()
    }
}