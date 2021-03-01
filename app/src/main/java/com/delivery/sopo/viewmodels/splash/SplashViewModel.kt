package com.delivery.sopo.viewmodels.splash

import android.net.Network
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.networks.call.OAuthCall
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class SplashViewModel(
    private val userRepoImpl : UserRepoImpl, private val oauthRepoImpl : OauthRepoImpl
) : ViewModel()
{
    val TAG = this.javaClass.simpleName

    var navigator = MutableLiveData<String>()

    init
    {
        navigator.value = NavigatorConst.TO_PERMISSION
    }

    fun requestAfterActivity()
    {
        SopoLog.d( msg = "로그인 상태 => ${userRepoImpl.getStatus()}")

        if (userRepoImpl.getStatus() == 1)
        {
            getUserInfoWithToken()
        }
        else
        {
            navigator.value = NavigatorConst.TO_INTRO
        }
    }

    private fun getUserInfoWithToken()
    {
        CoroutineScope(Dispatchers.IO).launch {
            when(val result = UserCall.getUserInfoWithToken())
            {
                is NetworkResult.Success ->
                {
                    SopoLog.d(msg = "User Info Call Success - ${result.data.toString()}")

                    val userDetail = result.data.data

                    userRepoImpl.setEmail(userDetail?.userName?:"")
                    userRepoImpl.setUserNickname(userDetail?.nickname?:"")
                    userRepoImpl.setJoinType(userDetail?.joinType?:"")

                    navigator.postValue(NavigatorConst.TO_MAIN)
                }
                is NetworkResult.Error ->
                {
                    navigator.postValue(NavigatorConst.TO_INTRO)
                    SopoLog.e(msg = "User Info Call Fai")
                }
            }
        }

    }

    private fun checkOAuthToken()
    {
        var oauth : OauthEntity?
        runBlocking {
            withContext(Dispatchers.Default){
                oauth = oauthRepoImpl.get(userRepoImpl.getEmail())
            }
        }

        if (oauth == null) return

        CoroutineScope(Dispatchers.IO).launch {
            when (val result = OAuthCall.checkOAuthToken(oauth!!.accessToken))
            {
                is NetworkResult.Success ->
                {
                    SopoLog.d(msg = "성공 => ${result.data}")
                }
                is NetworkResult.Error ->
                {
                    SopoLog.d(msg = "실패 => ${(result.exception as APIException)}")
                }
            }
        }
    }
}