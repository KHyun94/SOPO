package com.delivery.sopo.viewmodels.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.exceptions.APIBetaException
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class SplashViewModel(private val userLocalRepo: UserLocalRepository, private val userRemoteRepo: UserRemoteRepository,private val oAuthLocalRepo: OAuthLocalRepository): ViewModel()
{
    var navigator = MutableLiveData<String>()
    var errorMessage: String = ""

    private val _error = MutableLiveData<Pair<Int, ErrorResponse>>()
    val error: LiveData<Pair<Int, ErrorResponse>>
        get() = _error

    val handler = CoroutineExceptionHandler { context, exception ->
        when(exception)
        {
            is APIBetaException ->
            {
                _error.postValue(Pair(exception.getStatusCode(), exception.getErrorResponse()))
            }
            is InternalServerException ->
            {
                _error.postValue(Pair(500, exception.getErrorResponse()))
            }
        }
    }

    private val scope: CoroutineScope = viewModelScope

    init
    {
        checkUserStatus()
    }

    private fun checkUserStatus()
    {
        SopoLog.d(msg = "로그인 상태[${userLocalRepo.getStatus()}]")

        if (userLocalRepo.getStatus() == StatusConst.ACTIVATE)
        {
            navigator.postValue(NavigatorConst.TO_PERMISSION)
        }
        else
        {
            navigator.value = NavigatorConst.TO_INTRO
        }
    }

    suspend fun requestLoginStatusForKeeping() = scope.launch(Dispatchers.IO){

        try
        {
            val isExpired = isExpiredTokenWithinWeek()

            if(isExpired)
            {
                userRemoteRepo.requestLogin(userLocalRepo.getUserId(), userLocalRepo.getUserPassword())
            }

            val userInfo = userRemoteRepo.getUserInfo()

            if(userInfo.nickname == "")
            {
                navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
                return@launch
            }

        }
        catch(e: Exception)
        {
            handler.handleException(coroutineContext, e)
        }
    }

    suspend fun getUserInfoWithToken()
    {

/*
* if(responseCode == ResponseCode.TOKEN_ERROR_INVALID_GRANT || responseCode == ResponseCode.TOKEN_ERROR_INVALID_TOKEN)
                {
                    val oAuth =  oAuthLocalRepo.get(userLocalRepo.getUserId())

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
*
*
* */

    }


    /**
     * 토큰 만료일 기준 1주일 내외 일 때
     * 토큰을 새로 요청함
     *
     * true - 갱신 필요
     * false -갱신 필요 없음
     */
    suspend fun isExpiredTokenWithinWeek():Boolean = withContext(Dispatchers.Default) {
        SopoLog.i("isRefreshTokenWithinWeek() 호출")

        // 로컬 내 oAuth Token의 만료 기일을 로드
        val currentExpiredDate: String = withContext(Dispatchers.Default) {
            oAuthLocalRepo.get(userLocalRepo.getUserId())?.run { OAuthMapper.entityToObject(this).refreshTokenExpiredAt }?:""
        }

        SopoLog.d("O-Auth Token Expired Date(갱신 전) [data:$currentExpiredDate]")

        return@withContext DateUtil.isExpiredDateWithinAWeek(currentExpiredDate)
    }


}