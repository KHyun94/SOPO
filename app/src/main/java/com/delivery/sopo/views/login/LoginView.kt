package com.delivery.sopo.views.login

import android.content.Intent
import android.view.Gravity
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.extensions.launchActivityWithAllClear
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.login.LoginViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.RegisterNicknameView
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginView: BaseView<LoginViewBinding, LoginViewModel>()
{
    private var progressBar: CustomProgressBar? = CustomProgressBar(this@LoginView)

    override val layoutRes: Int = R.layout.login_view
    override val vm: LoginViewModel by viewModel()

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
        vm.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this@LoginView, focus)
            vm.validity[res.first] = res.second
        })

        vm.invalidity.observe(this) { target ->
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
        }

        vm.errorCode.observe(this){ code ->
            when(code)
            {
                ResponseCode.TOKEN_ERROR_VALIDATION ->
                {
                    /*Toast.makeText(this@LoginView,message, Toast.LENGTH_LONG).apply {
                        setGravity(Gravity.TOP, 0, 180)
                    }.show()*/
                    CustomSnackBar.make(view = binding.layoutInput,
                                        content = "이메일 또는 비밀번호를 확인해주세요.",
                                        duration = 3000,
                                        type = SnackBarEnum.ERROR).show()
                }
                else ->
                {
                    CustomSnackBar.make(view = binding.layoutInput,
                                        content = "알 수 없는 서버 에러입니다.",
                                        duration = 3000,
                                        type = SnackBarEnum.ERROR).show()
                }
            }
        }

        vm.isProgress.observe(this, Observer { isProgress ->
            if (isProgress == null) return@Observer

            if (progressBar == null)
            {
                progressBar = CustomProgressBar(this)
            }

            progressBar?.onStartProgress(isProgress) { isDismiss ->
                if (isDismiss) progressBar = null
            }
        })

        vm.navigator.observe(this@LoginView, Observer { navigator ->
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