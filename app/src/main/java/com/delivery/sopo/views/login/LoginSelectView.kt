package com.delivery.sopo.views.login

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.LoginSelectViewBinding
import com.delivery.sopo.extensions.launchActivityWithAllClear
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.SignUpView
import com.delivery.sopo.views.signup.RegisterNicknameView
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.util.exception.KakaoException
import kotlinx.android.synthetic.main.login_select_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginSelectView : BasicView<LoginSelectViewBinding>(R.layout.login_select_view)
{
    private val vm : LoginSelectViewModel by viewModel()

    private var sessionCallback : ISessionCallback? = null
    var progressBar : CustomProgressBar? = null

    init
    {
        parentActivity = this@LoginSelectView
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun bindView()
    {
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    override fun setObserver()
    {
        vm.navigator.observe(this, Observer { navigator ->

            SopoLog.d("""
                Navigator >>> ${navigator}
            """.trimIndent())

            when (navigator)
            {
                NavigatorConst.TO_LOGIN ->
                {
                    startActivity(Intent(parentActivity, LoginView::class.java))
                }
                NavigatorConst.TO_SIGN_UP ->
                {
                    startActivity(Intent(parentActivity, SignUpView::class.java))
                }
                NavigatorConst.TO_KAKAO_LOGIN ->
                {
                    btn_kakao_login.performClick()

                    if (Session.getCurrentSession() != null) Session.getCurrentSession().removeCallback(sessionCallback)

                    sessionCallback = object : ISessionCallback
                    {
                        override fun onSessionOpened()
                        {
                            binding.vm!!.requestKakaoLogin()
                        }

                        override fun onSessionOpenFailed(exception : KakaoException)
                        {
                            SopoLog.e( msg = "카카오 세션 에러: ${exception}", e = exception)
                        }
                    }
                    Session.getCurrentSession().addCallback(sessionCallback)
                }
                NavigatorConst.TO_MAIN ->
                {
                    Intent(this, MainView::class.java).launchActivityWithAllClear(this@LoginSelectView)
                }
                NavigatorConst.TO_UPDATE_NICKNAME ->
                {
                    Intent(this, RegisterNicknameView::class.java).launchActivityWithAllClear(this@LoginSelectView)
                }
            }

        })

        binding.vm!!.isProgress.observe(this, Observer { isProgress ->
            if(isProgress == null) return@Observer

            if(progressBar == null)
            {
                progressBar = CustomProgressBar(this)
            }

            progressBar?.onStartProgress(isProgress){isDismiss ->
                if(isDismiss) progressBar = null
            }

        })

        binding.vm!!.result.observe(this, Observer { result ->

//            if (it.successResult != null)
//            {
//                SopoLog.d(msg = "성공 발생 => ${it.successResult}")
//
//                val data = it.successResult!!.data
//
//                if (data != null)
//                {
//                    val intent = Intent(parentActivity, MainView::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//                    finish()
//                }
//            }
//
//            if (it.errorResult != null)
//            {
//                SopoLog.e(msg = "에러 발생 => ${it.errorResult}")
//
//                when (val type = it.errorResult!!.errorType)
//                {
//                    ErrorResult.ERROR_TYPE_NON -> return@Observer
//                    ErrorResult.ERROR_TYPE_TOAST ->
//                    {
//                        CustomAlertMsg.floatingUpperSnackBAr(
//                            context = parentActivity, msg = it.errorResult!!.errorMsg, isClick = true
//                        )
//                        return@Observer
//                    }
//                    ErrorResult.ERROR_TYPE_DIALOG ->
//                    {
//                        val code = it.errorResult!!.code?.CODE
//                        val data = it.errorResult!!.data
//                        val msg = it.errorResult!!.errorMsg
//
//                        SopoLog.e(msg = "이시발 뭐지 ${data}")
//
//                        GeneralDialog(
//                            act = parentActivity, title = "오류", msg = msg, detailMsg = code, rHandler = Pair(first = "네", second = null)
//                        ).show(supportFragmentManager, "tag")
//                    }
//                    ErrorResult.ERROR_TYPE_SCREEN -> return@Observer
//                    else -> return@Observer
//                }
//            }
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