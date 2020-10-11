package com.delivery.sopo.viewmodels.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.repository.impl.UserRepoImpl

class SplashViewModel(
    private val userRepoImpl: UserRepoImpl
) : ViewModel()
{
    var navigator = MutableLiveData<String>()

    init
    {
        navigator.value = NavigatorConst.TO_PERMISSION
    }

    fun requestAfterActivity()
    {
        if (userRepoImpl.getStatus() == 1)
        {
            navigator.value = NavigatorConst.TO_MAIN
        }
        else
        {
            navigator.value = NavigatorConst.TO_INTRO
        }
    }
}