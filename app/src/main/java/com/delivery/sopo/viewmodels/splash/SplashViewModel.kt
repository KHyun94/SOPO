package com.delivery.sopo.viewmodels.splash

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class SplashViewModel(
        private val userLocalRepo: UserLocalRepository,
        private val userRemoteRepo: UserRemoteRepository,
        private val oAuthLocalRepo: OAuthLocalRepository): BaseViewModel()
{
    var navigator = MutableLiveData<String>()

    private val onSOPOErrorCallback = object : OnSOPOErrorCallback{

        override fun onFailure(error: ErrorEnum)
        {
            when(error)
            {
                ErrorEnum.NICK_NAME_NOT_FOUND ->
                {
                    navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
                }
                else ->
                {
                    navigator.postValue(NavigatorConst.TO_INTRO)
                }
            }
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
            navigator.postValue(NavigatorConst.TO_INTRO)
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)
            navigator.postValue(NavigatorConst.TO_INTRO)
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }

    init
    {
        Handler(Looper.getMainLooper()).postDelayed(Runnable { checkUserStatus() }, 1500)
    }

    private fun checkUserStatus()
    {
        SopoLog.i(msg = "checkUserStatus(...) 호출 [로그인 상태:${userLocalRepo.getStatus()}]")

        if(userLocalRepo.getStatus() == StatusConst.ACTIVATE)
        {
            return navigator.postValue(NavigatorConst.TO_PERMISSION)
        }

        navigator.postValue(NavigatorConst.TO_INTRO)
    }

    fun requestUserInfo() = checkEventStatus(true) {

        scope.launch {
            try
            {
                val isExpired = isExpiredTokenWithinWeek()

                if(isExpired)
                {
                    SopoLog.d("만료 직전 강제 로그인 요청")
                    userRemoteRepo.requestLogin(userLocalRepo.getUserId(), userLocalRepo.getUserPassword())
                }

                val userInfo = userRemoteRepo.getUserInfo()
                SopoLog.d("로그인 성공 [UserInfo:${userInfo.toString()}]")

                if(userInfo.nickname == "") return@launch navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)

                navigator.postValue(NavigatorConst.TO_MAIN)
            }
            catch(e: Exception)
            {
                exceptionHandler.handleException(coroutineContext, e)
            }
        }

    }

    /**
     * 토큰 만료일 기준 1주일 내외 일 때
     * 토큰을 새로 요청함
     *
     * true - 갱신 필요
     * false -갱신 필요 없음
     */
    private suspend fun isExpiredTokenWithinWeek(): Boolean = withContext(Dispatchers.Default) {
        SopoLog.i("checkExpiredTokenWithinWeek() 호출")

        // 로컬 내 oAuth Token의 만료 기일을 로드
        val currentExpiredDate: String = oAuthLocalRepo.get(userLocalRepo.getUserId()).refreshTokenExpiredAt
        return@withContext DateUtil.isExpiredDateWithinAWeek(currentExpiredDate)
    }
}