package com.delivery.sopo.views.login

import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.networks.handler.LoginHandler
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.login.LoginViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginView : BasicView<LoginViewBinding>(R.layout.login_view)
{
    private val loginVm: LoginViewModel by viewModel()
    private var progressBar: CustomProgressBar? = null

    override fun bindView()
    {
        binding.vm = loginVm
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
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
                    Toast.makeText(this, "성공", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@LoginView, MainView::class.java))
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
                            context = parentActivity,
                            msg = it.errorResult!!.errorMsg,
                            isClick = true
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
                                    act = parentActivity,
                                    title = "오류",
                                    msg = msg,
                                    detailMsg = code,
                                    rHandler = Pair(
                                        first = "재전송",
                                        second = { it ->
                                            FirebaseRepository.firebaseSendEmail(SOPOApp.auth.currentUser) { success, error ->
                                               binding.vm!!.postResultValue(success, error)
                                            }
                                            it.dismiss()
                                        }),
                                    lHandler = Pair(first = "확인", second = null)
                                ).show(supportFragmentManager, "tag")
                            }
                            ResponseCode.ALREADY_LOGGED_IN ->
                            {
                                // todo 중복 로그인 처리
                                val jwtToken = data as String

                                GeneralDialog(
                                    act = parentActivity,
                                    title = "오류",
                                    msg = msg,
                                    detailMsg = code,
                                    rHandler = Pair(
                                        first = "네",
                                        second = { it ->
                                            LoginHandler.authJwtToken(jwtToken){ successResult, errorResult ->
                                                if(errorResult != null) SopoLog.e(msg = "에러 $errorResult")
                                                if(successResult != null) SopoLog.d(msg = "성공 $successResult")

                                                it.dismiss()
                                            }

                                        }),
                                    lHandler = Pair(first = "아니오", second = null)
                                ).show(supportFragmentManager, "tag")
                            }
                            else ->
                            {
                                GeneralDialog(
                                    act = parentActivity,
                                    title = "오류",
                                    msg = msg,
                                    detailMsg = code,
                                    rHandler = Pair(first = "네", second = null)
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
}