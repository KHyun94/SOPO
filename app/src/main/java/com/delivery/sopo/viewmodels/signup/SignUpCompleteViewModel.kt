package com.delivery.sopo.viewmodels.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.repository.OAuthNetworkRepo
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpCompleteViewModel(private val userRepo: UserRepoImpl, private val oAuthRepo: OauthRepoImpl): ViewModel()
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
        login(userRepo.getEmail(), userRepo.getApiPwd())
    }

    fun login(email: String, password: String)
    {
        SopoLog.d("""
            login() call
            email >>> $email
            password >>> $password
        """.trimIndent())

        CoroutineScope(Dispatchers.Main).launch {

            val oAuthRes = OAuthNetworkRepo.loginWithOAuth(email, password)

            SopoLog.d("""
                OAuth Login >>> 
                ${oAuthRes.result}
                ${oAuthRes.message}
                ${oAuthRes.data}
            """.trimIndent())

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

            withContext(Dispatchers.Default){
                oAuthRepo.insert(oAuthRes.data!!)
            }

            SopoLog.d("Nickname is ${userRepo.getNickname()}")

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