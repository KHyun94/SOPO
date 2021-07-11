package com.delivery.sopo.views.login

import android.content.Intent
import android.view.Gravity
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.extensions.launchActivityWithAllClear
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.login.LoginViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.RegisterNicknameView
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginView: BasicView<LoginViewBinding>(R.layout.login_view)
{
    private val loginVm: LoginViewModel by viewModel()
    private var progressBar: CustomProgressBar? = CustomProgressBar(this@LoginView)

    override fun bindView()
    {
        binding.vm = loginVm
        binding.executePendingBindings()
    }


    override fun setObserver()
    {

        binding.vm!!.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this@LoginView, focus)
            binding.vm!!.validities[res.first] = res.second
        })

        binding.vm!!.invalidity.observe(this, Observer { target ->
            val message = when(target.first)
            {
                InfoEnum.EMAIL ->
                {
                    binding.etEmail.requestFocus()
                    "이메일을 확인해주세요."
                }
                InfoEnum.PASSWORD ->
                {
                    binding.etPassword.requestFocus()
                    "비밀번호 확인해주세요."
                }
                else -> ""
            }

            Toast.makeText(this@LoginView,message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        })

        binding.vm!!.isProgress.observe(this, Observer { isProgress ->
            if (isProgress == null) return@Observer

            if (progressBar == null)
            {
                progressBar = CustomProgressBar(this)
            }

            progressBar?.onStartProgress(isProgress) { isDismiss ->
                if (isDismiss) progressBar = null
            }
        })

        binding.vm!!.result.observe(this, Observer { result ->

            SopoLog.d(
                """
                Self Login Result >>> ${result.message}                
                ${result.result}
                ${result.code}
                ${result.data}
                ${result.displayType}

            """.trimIndent()
            )

            if (!result.result)
            {
                when (result.displayType)
                {
                    DisplayEnum.TOAST_MESSAGE ->
                    {
                        CustomAlertMsg.floatingUpperSnackBAr(context = this@LoginView, msg = result.message, isClick = true)
                    }
                    DisplayEnum.DIALOG ->
                    {
                        GeneralDialog(
                            act = this@LoginView, title = "오류", msg = result.message, detailMsg = result.code?.CODE, rHandler = Pair(first = "네", second = object: OnAgreeClickListener{
                                override fun invoke(agree: GeneralDialog)
                                {
                                    agree.dismiss()
                                }

                            })
                        ).show(supportFragmentManager, "tag")
                    }
                    else -> return@Observer
                }

                return@Observer
            }
        })

        binding.vm!!.navigator.observe(this@LoginView, Observer { navigator ->
            when (navigator)
            {
                NavigatorConst.TO_RESET_PASSWORD ->
                {
                    Intent(this@LoginView, ResetPasswordView::class.java).launchActivity(this@LoginView)
                }
                NavigatorConst.TO_UPDATE_NICKNAME ->
                {
                    Intent(this@LoginView, RegisterNicknameView::class.java).launchActivityWithAllClear(this@LoginView)
                }
                NavigatorConst.TO_MAIN ->
                {
                    Intent(this@LoginView, MainView::class.java).launchActivityWithAllClear(this@LoginView)
                }
            }
        })
    }
}