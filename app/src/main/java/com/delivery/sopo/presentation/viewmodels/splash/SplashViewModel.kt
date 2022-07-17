package com.delivery.sopo.presentation.viewmodels.splash

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.domain.usecase.user.token.ForceLoginUseCase
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class SplashViewModel(
        private val forceLoginUseCase: ForceLoginUseCase,
        private val userDataSource: UserDataSource,
        private val carrierDataSource: CarrierDataSource): BaseViewModel()
{
    init
    {
        scope.launch(Dispatchers.Default) {
            carrierDataSource.initCarrierTable()
            carrierDataSource.initCarrierPatternTable()
        }

    }

    private var _navigator = MutableLiveData<String>()
    val navigator : LiveData<String> = _navigator

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)

        when(exception.code)
        {
            ErrorCode.NICK_NAME_NOT_FOUND -> postNavigator(NavigatorConst.Screen.UPDATE_NICKNAME)
            else -> postNavigator(NavigatorConst.TO_INTRO)
        }
    }

    override fun handlerInternalServerException(exception: InternalServerException)
    {
        super.handlerInternalServerException(exception)

        postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        postNavigator(NavigatorConst.TO_INTRO)
    }

    override fun handlerException(exception: Exception)
    {
        super.handlerException(exception)
        postErrorSnackBar("[불명] ${exception.toString()}")
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
        scope.launch {
                forceLoginUseCase.invoke()
                postNavigator(NavigatorConst.Screen.MAIN)
        }
    }
}