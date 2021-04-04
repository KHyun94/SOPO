package com.delivery.sopo.views.login

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.databinding.LoginSelectViewBinding
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.extensions.launchActivitiy
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.networks.call.OAuthCall
import com.delivery.sopo.networks.handler.LoginHandler
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.SignUpStep2View
import com.delivery.sopo.views.signup.SignUpView
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.util.exception.KakaoException
import kotlinx.android.synthetic.main.login_select_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginSelectView : BasicView<LoginSelectViewBinding>(R.layout.login_select_view)
{
    var TAG = ""

    private val userRepoImpl : UserRepoImpl by inject()
    private val oauthRepoImpl : OauthRepoImpl by inject()
    private val loginSelectVm : LoginSelectViewModel by viewModel()

    private var sessionCallback : ISessionCallback? = null
    var progressBar : CustomProgressBar? = null

    init
    {
        TAG += this.javaClass.simpleName
        parentActivity = this@LoginSelectView
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun bindView()
    {
        binding.vm = loginSelectVm
        binding.lifecycleOwner = this
    }

    override fun setObserver()
    {
        loginSelectVm.loginType.observe(this, Observer {

            when (it)
            {
                NavigatorConst.LOGIN ->
                {
                    startActivity(
                        Intent(parentActivity, LoginView::class.java)
                    )
                }
                NavigatorConst.SIGN_UP ->
                {
//                    startActivity(Intent(parentActivity, SignUpView::class.java))
                    startActivity(Intent(parentActivity, SignUpStep2View::class.java))
                }
                NavigatorConst.KAKAO_LOGIN ->
                {
                    btn_kakao_login.performClick()

                    if (Session.getCurrentSession() != null) Session.getCurrentSession()
                        .removeCallback(sessionCallback)

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

        binding.vm!!.result.observe(this, Observer {

            if (it == null) return@Observer

            if (it.successResult != null)
            {
                SopoLog.d(msg = "성공 발생 => ${it.successResult}")

                val data = it.successResult!!.data

                if (data != null)
                {
                    val intent = Intent(parentActivity, MainView::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }

            if (it.errorResult != null)
            {
                SopoLog.e(msg = "에러 발생 => ${it.errorResult}")

                when (val type = it.errorResult!!.errorType)
                {
                    ErrorResult.ERROR_TYPE_NON -> return@Observer
                    ErrorResult.ERROR_TYPE_TOAST ->
                    {
                        CustomAlertMsg.floatingUpperSnackBAr(
                            context = parentActivity, msg = it.errorResult!!.errorMsg, isClick = true
                        )
                        return@Observer
                    }
                    ErrorResult.ERROR_TYPE_DIALOG ->
                    {
                        val code = it.errorResult!!.code?.CODE
                        val data = it.errorResult!!.data
                        val msg = it.errorResult!!.errorMsg

                        SopoLog.e(msg = "이시발 뭐지 ${data}")

                        when (it.errorResult!!.code)
                        {
                            ResponseCode.FIREBASE_ERROR_EMAIL_VERIFIED ->
                            {
                                // todo 이메일 인증 관련 다이얼로그
                                GeneralDialog(
                                    act = parentActivity, title = "오류", msg = msg, detailMsg = code, rHandler = Pair(first = "재전송", second = { it ->
                                        FirebaseRepository.firebaseSendEmail(SOPOApp.auth.currentUser) { success, error ->
                                            binding.vm!!.postResultValue(success, error)
                                        }
                                        it.dismiss()
                                    }), lHandler = Pair(first = "확인", second = null)
                                ).show(supportFragmentManager, "tag")
                            }
                            ResponseCode.ALREADY_LOGGED_IN ->
                            {
                                // todo 중복 로그인 처리
                                val jwtToken = data as String

                                GeneralDialog(
                                    act = parentActivity, title = "오류", msg = msg, detailMsg = code, rHandler = Pair(first = "네", second = { it ->
                                        LoginHandler.authJwtToken(jwtToken) { successResult, errorResult ->
                                            if (errorResult != null) SopoLog.e(msg = "에러 ${errorResult.toString()}")
                                            if (successResult != null)
                                            {
                                                SopoLog.d(msg = "성공 ${successResult.toString()}")
                                                binding.vm!!.postResultValue(successResult, errorResult)
                                            }

                                            it.dismiss()
                                        }

                                    }), lHandler = Pair(first = "아니오", second = null)
                                ).show(supportFragmentManager, "tag")
                            }
                            else ->
                            {
                                GeneralDialog(
                                    act = parentActivity, title = "오류", msg = msg, detailMsg = code, rHandler = Pair(first = "네", second = null)
                                ).show(supportFragmentManager, "tag")
                            }
                        }
                    }
                    ErrorResult.ERROR_TYPE_SCREEN -> return@Observer
                    else -> return@Observer
                }
            }
        })
    }

    fun checkOAuthToken()
    {
        CoroutineScope(Dispatchers.IO).launch {

            var oauth : OauthEntity

            withContext(Dispatchers.Default) {
                oauth = oauthRepoImpl.get("asle1221@naver.com")!!
            }

            when (val result = OAuthCall.checkOAuthToken(oauth.accessToken))
            {
                is NetworkResult.Success ->
                {
                    SopoLog.d( msg = "성공 => ${result.data}")
                }
                is NetworkResult.Error ->
                {
                    SopoLog.d( msg = "성공 => ${(result.exception as APIException)}")
                }
            }
        }
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