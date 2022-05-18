package com.delivery.sopo.presentation.viewmodels.splash

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repositories.local.repository.CarrierRepository
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.domain.usecase.user.token.ForceLoginUseCase
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class SplashViewModel(
        private val forceLoginUseCase: ForceLoginUseCase,
        private val userDataSource: UserDataSource,
        private val carrierRepo: CarrierRepository): BaseViewModel()
{
    init
    {
        CoroutineScope(Dispatchers.Default).launch {
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

        override fun onFailure(error: ErrorEnum)
        {
            when(error)
            {
                ErrorEnum.NICK_NAME_NOT_FOUND ->
                {
                    postNavigator(NavigatorConst.TO_UPDATE_NICKNAME)
                }
                else ->
                {
                    postNavigator(NavigatorConst.TO_INTRO)
                }
            }
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
            postNavigator(NavigatorConst.TO_INTRO)
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)
            postNavigator(NavigatorConst.TO_INTRO)
        }

        override fun onDuplicateError(error: ErrorEnum)
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