package com.delivery.sopo.viewmodels.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.repository.OAuthNetworkRepo
import com.delivery.sopo.repository.impl.UserRepoImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpCompleteViewModel(private val userRepo: UserRepoImpl): ViewModel()
{
    val email = MutableLiveData<String>().also {
        it.postValue(userRepo.getEmail())
    }

    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

//    private var _navigator = MutableLiveData<String>()
//    val navigator: LiveData<String>
//        get() = _navigator

    val navigator = MutableLiveData<String>()

    val isProgress = MutableLiveData<Boolean?>()

    fun onCompleteClicked()
    {

    }

    fun login(email: String, password: String)
    {
        CoroutineScope(Dispatchers.Main).launch {
            val oAuthRes = OAuthNetworkRepo.loginWithOAuth(email, password)

            if(!oAuthRes.result)
            {
                return@launch
            }

            userRepo.run {
                setEmail(email)
                setApiPwd(password)
                setStatus(1)
            }

            SOPOApp.oAuthEntity = oAuthRes.data

            if(userRepo.getNickname() == "")
            {
                val infoRes = OAuthNetworkRepo.getUserInfo()

                if(!infoRes.result)
                {
                    _result.postValue(infoRes)
                    return@launch
                }

                if(infoRes.data == null || infoRes.data.nickname == "")
                {
                    navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
                    return@launch
                }

                userRepo.setNickname(infoRes.data.nickname)

                navigator.postValue(NavigatorConst.TO_MAIN)
            }
        }
    }
}