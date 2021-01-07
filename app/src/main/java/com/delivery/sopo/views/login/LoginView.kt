package com.delivery.sopo.views.login

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.models.ErrorResult
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
    private var progressBar : CustomProgressBar? = null

    init
    {
        TAG += this.javaClass.simpleName
        parentActivity = this@LoginView
        progressBar = CustomProgressBar(this)
    }

    override fun bindView()
    {
        binding.vm = loginVm
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
        binding.vm!!.isProgress.observe(this, Observer {
            if(it == null) return@Observer

            if(progressBar == null) progressBar = CustomProgressBar(this)

            if(it)
            {
                progressBar!!.onStartDialog()
            }
            else
            {
                progressBar!!.onCloseDialog()
                progressBar = null
            }
        })

        binding.vm!!.result.observe(this, Observer {

            if (it == null) return@Observer

            if (it.successResult != null)
            {
                SopoLog.d(msg = "성공 발생 => ${it.successResult}")
                if (it.successResult!!.data != null)
                {
                    startActivity(Intent(this@LoginView, MainView::class.java))
                    finish()
                }
            }
            else if (it.errorResult != null)
            {
                SopoLog.e(msg = "에러 발생 => ${it.errorResult}")
                when (it.errorResult!!.errorType)
                {
                    ErrorResult.ERROR_TYPE_NON -> { }
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
                        var msg = ""
                        val code = it.errorResult!!.codeEnum?.CODE

                        when (it.errorResult!!.data)
                        {
                            is Int -> msg = getString(it.errorResult!!.data as Int)
                            is String ->
                            {
                                val jwtToken = it.errorResult!!.data as String

                                msg = it.errorResult!!.errorMsg

                                GeneralDialog(
                                    act = parentActivity,
                                    title = "오류",
                                    msg = msg,
                                    detailMsg = code,
                                    rHandler = Pair(
                                        first = "네",
                                        second = { it ->
                                            binding.vm!!.authJwtToken(jwtToken = jwtToken)
                                            it.dismiss()
                                        }),
                                    lHandler = Pair(first = "아니오", second = null)
                                ).show(supportFragmentManager, "tag")

                                return@Observer
                            }
                            else -> msg = it.errorResult!!.errorMsg
                        }

                        GeneralDialog(
                            act = parentActivity,
                            title = "오류",
                            msg = msg,
                            detailMsg = code,
                            rHandler = Pair(first = "네", second = null)
                        ).show(supportFragmentManager, "tag")
                    }
                    ErrorResult.ERROR_TYPE_SCREEN ->
                    {
                    }
                    else ->
                    {
                    }
                }
            }
        })
    }
}