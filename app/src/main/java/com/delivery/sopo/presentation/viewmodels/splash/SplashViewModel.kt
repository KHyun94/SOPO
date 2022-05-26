package com.delivery.sopo.presentation.viewmodels.splash

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.models.AuthToken
import com.delivery.sopo.data.repositories.local.repository.CarrierRepository
import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.domain.usecase.user.token.ForceLoginUseCase
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import org.koin.core.inject

class SplashViewModel(
        private val forceLoginUseCase: ForceLoginUseCase,
        private val userDataSource: UserDataSource,
        private val carrierRepo: CarrierRepository): BaseViewModel()
{
    private val authDataSource: AuthDataSource by inject()

    init
    {
//        val authToken = AuthToken.Info(grantType = "bearer", userToken = "user_JotbTyyS0FuavZJ", accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyX0pvdGJUeXlTMEZ1YXZaSiIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE2NTM1NjY5NDEsImV4cCI6MTY1MzY1MzM0MX0.91KspEWnWydRa_WEr14Ey2vMxZ-mcJq2uhRPSuOY9I8", refreshToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyX0pvdGJUeXlTMEZ1YXZaSiIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE2NTM1NjY5NDEsImV4cCI6MTY1NDc3NjU0MX0.tthcOkpcL2P5DhirpRyWdDYRzqU1C0KsPVhLo6ehypo", expireAt = "2022-06-09T21:09:01.944+09:00[Asia/Seoul]")

        CoroutineScope(Dispatchers.Default).launch {

//            authDataSource.insert(authToken)
//
//            userDataSource.setUsername("asle1221@naver.com")
//            userDataSource.setUserPassword("EA028BB58781D1772CC1BAAE6518BFFE")
//            userDataSource.setStatus(1)

            carrierRepo.initCarrierDB()
        }
    }

    private var _navigator = MutableLiveData<String>()
    val navigator : LiveData<String>
    get() = _navigator

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    override var onSOPOErrorCallback = object : OnSOPOErrorCallback{

        override fun onFailure(error: ErrorCode)
        {
            when(error)
            {
                ErrorCode.NICK_NAME_NOT_FOUND ->
                {
                    postNavigator(NavigatorConst.TO_UPDATE_NICKNAME)
                }
                else ->
                {
                    postNavigator(NavigatorConst.TO_INTRO)
                }
            }
        }

        override fun onInternalServerError(error: ErrorCode)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
            postNavigator(NavigatorConst.TO_INTRO)
        }

        override fun onAuthError(error: ErrorCode)
        {
            super.onAuthError(error)
            postNavigator(NavigatorConst.TO_INTRO)
        }

        override fun onDuplicateError(error: ErrorCode)
        {
            super.onDuplicateError(error)

            postNavigator(NavigatorConst.DUPLICATE_LOGIN)
        }
    }

    init
    {
        Handler(Looper.getMainLooper()).postDelayed(Runnable { checkUserStatus() }, 1500)
    }

    private fun checkUserStatus()
    {
        SopoLog.i(msg = "checkUserStatus(...) 호출 [로그인 상태:${userDataSource.getStatus()}]")

        if(userDataSource.getStatus() == StatusConst.ACTIVATE)
        {
            return postNavigator(NavigatorConst.TO_PERMISSION)
        }

        postNavigator(NavigatorConst.TO_INTRO)
    }

    fun requestUserInfo() = checkEventStatus(true) {



        scope.launch(coroutineExceptionHandler) {
                forceLoginUseCase.invoke()
                postNavigator(NavigatorConst.TO_MAIN)
        }
    }
}