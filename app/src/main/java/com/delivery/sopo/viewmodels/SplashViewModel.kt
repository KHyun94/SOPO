package com.delivery.sopo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.repository.UserRepo

class SplashViewModel(
    private val userRepo: UserRepo
) : ViewModel()
{

    var navigator = MutableLiveData<String>()

    init
    {
        navigator.value = NavigatorConst.TO_PERMISSION
    }

    fun requestAfterActivity()
    {
        if (userRepo.getStatus() == 1)
        {
            navigator.value = NavigatorConst.TO_MAIN
        }
        else
        {
            navigator.value = NavigatorConst.TO_INTRO
        }
    }
}