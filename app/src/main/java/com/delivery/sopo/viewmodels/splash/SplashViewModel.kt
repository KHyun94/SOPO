package com.delivery.sopo.viewmodels.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SplashViewModel(private val userRepoImpl: UserRepoImpl, private val oauthRepoImpl: OauthRepoImpl): ViewModel()
{
    var navigator = MutableLiveData<String>()
    var errorMessage: String = ""

    init
    {
        navigator.value = NavigatorConst.TO_PERMISSION
    }

    fun requestAfterActivity()
    {
        SopoLog.d(msg = "로그인 상태 >>> ${userRepoImpl.getStatus()}")

        if (userRepoImpl.getStatus() == 1)
        {
            CoroutineScope(Dispatchers.IO).launch { getUserInfoWithToken() }
        }
        else
        {
            navigator.value = NavigatorConst.TO_INTRO
        }
    }

    private suspend fun getUserInfoWithToken()
    {
        when (val result = UserCall.getUserInfoWithToken())
        {
            is NetworkResult.Success ->
            {
                SopoLog.d(msg = "User Info Call Success - ${result.data.toString()}")

                val userDetail = result.data.data

                userRepoImpl.setEmail(userDetail?.userName ?: "")
                userRepoImpl.setNickname(userDetail?.nickname ?: "")
                userRepoImpl.setJoinType(userDetail?.joinType ?: "")

                navigator.postValue(NavigatorConst.TO_MAIN)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val responseCode = exception.responseCode

                if(responseCode == ResponseCode.TOKEN_ERROR_INVALID_GRANT || responseCode == ResponseCode.TOKEN_ERROR_INVALID_TOKEN)
                {
                    val oAuth =  oauthRepoImpl.get(userRepoImpl.getEmail())

                    val isOver = DateUtil.isOverExpiredDate(oAuth?.refreshTokenExpiredAt?:"")

                    // TODO 문구 정해야함
                   if(isOver)
                    {
                        // 만료일이 지난 케이스, 로그인 기한이 지났으니 다시 로그인하세요/

                        SopoLog.e("만료일 over")
                        navigator.postValue(NavigatorConst.TO_INIT)
                        errorMessage = "로그인 기한이 지났습니다.\n다시 로그인해주세요."
                        return
                    }

                    // 만료일이 지나지 않은 케이스ㅣ, but 토큰이 만료 누군가 로그인했을지도 모른다 그러므로 종료

                    SopoLog.e("중복 로그인")

                    navigator.postValue(NavigatorConst.TO_INIT)
                    errorMessage = "다른 곳에서 로그인했을 수도 있습니다.\n다시 로그인해주세요."

                    return
                }

                navigator.postValue(NavigatorConst.TO_INTRO)
                SopoLog.e(msg = "User Info Call Fai")
            }
        }

    }
}