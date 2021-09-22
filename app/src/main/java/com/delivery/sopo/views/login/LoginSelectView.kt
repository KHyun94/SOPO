package com.delivery.sopo.views.login

import android.content.Intent
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.LoginSelectViewBinding
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.extensions.launchActivityWithAllClear
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.RegisterNicknameView
import com.delivery.sopo.views.signup.SignUpView
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.util.exception.KakaoException
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginSelectView : BaseView<LoginSelectViewBinding, LoginSelectViewModel>()
{
    override val layoutRes: Int=R.layout.login_select_view
    override val vm : LoginSelectViewModel by viewModel()

    private var sessionCallback : ISessionCallback? = null
    var progressBar : CustomProgressBar? = null

    override fun receivedData(intent: Intent)
    {
    }

    override fun initUI()
    {
    }

    override fun setAfterSetUI()
    {
    }

    override fun setObserve()
    {
        vm.errorCode.observe(this) { code ->

            when(code)
            {
                ResponseCode.FAIL_TO_LOGIN_KAKAO ->
                {
                    CustomSnackBar.make(view = binding.layoutLoginSelect,
                                        content = "카카오 로그인에 실패했습니다.",
                                        duration = 3000,
                                        type = SnackBarEnum.ERROR).show()
                }
                else ->
                {
                    CustomSnackBar.make(view = binding.layoutLoginSelect,
                                        content = "알 수 없는 에러입니다.",
                                        duration = 3000,
                                        type = SnackBarEnum.ERROR).show()
                }
            }
        }

        vm.navigator.observe(this, Observer { navigator ->

            when (navigator)
            {
                NavigatorConst.TO_LOGIN ->
                {
                    startActivity(Intent(this, LoginView::class.java))
                }
                NavigatorConst.TO_SIGN_UP ->
                {
                    startActivity(Intent(this, SignUpView::class.java))
                }
                NavigatorConst.TO_MAIN ->
                {
                    Intent(this, MainView::class.java).launchActivityWithAllClear(this@LoginSelectView)
                }
                NavigatorConst.TO_UPDATE_NICKNAME ->
                {
                    Intent(this, RegisterNicknameView::class.java).launchActivityWithAllClear(this@LoginSelectView)
                }
                NavigatorConst.TO_KAKAO_LOGIN ->
                {
                    binding.btnKakaoLogin.performClick()

                    if (Session.getCurrentSession() != null) Session.getCurrentSession().removeCallback(sessionCallback)

                    sessionCallback = object : ISessionCallback
                    {
                        override fun onSessionOpened()
                        {
                            vm.requestKakaoLogin()
                        }

                        override fun onSessionOpenFailed(exception : KakaoException)
                        {
                            SopoLog.e( msg = "카카오 세션 에러: ${exception}", e = exception)
                        }
                    }
                    Session.getCurrentSession().addCallback(sessionCallback)
                }
            }
        })

       vm.isProgress.observe(this, Observer { isProgress ->
            if(isProgress == null) return@Observer

            if(progressBar == null)
            {
                progressBar = CustomProgressBar(this)
            }

            progressBar?.onStartProgress(isProgress){isDismiss ->
                if(isDismiss) progressBar = null
            }

        })
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) return
        super.onActivityResult(requestCode, resultCode, data)
    }



    override fun onDestroy()
    {
        super.onDestroy()
        if (sessionCallback != null) Session.getCurrentSession().removeCallback(sessionCallback)
    }


}