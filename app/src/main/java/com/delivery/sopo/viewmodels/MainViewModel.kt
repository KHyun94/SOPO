package com.delivery.sopo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.util.fun_util.SingleLiveEvent

class MainViewModel(
    private val userRepo: UserRepo
) : ViewModel()
{
    val tabLayoutVisibility = MutableLiveData<Int>()
    val errorMsg = MutableLiveData<String>()

    init
    {
        setPrivateUserAccount()
        errorMsg.value = ""
    }



    fun setTabLayoutVisiblity(visibility: Int)
    {
        tabLayoutVisibility.value = visibility
    }

    // network private api account setting. if it failed, try to logout and finish
    private fun setPrivateUserAccount()
    {
        if(userRepo.getStatus() != 1)
            NetworkManager.initPrivateApi(userRepo.getEmail(), userRepo.getApiPwd())
        else
            errorMsg.value = "로그인이 비정상적으로 이루어졌습니다.\n다시 로그인해주시길 바랍니다."
    }
}