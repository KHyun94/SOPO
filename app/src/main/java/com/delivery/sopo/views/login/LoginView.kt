package com.delivery.sopo.views.login

import android.content.Intent
import android.view.Gravity
import android.view.View
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
    override val layoutRes: Int = R.layout.login_view
    override val vm: LoginViewModel by viewModel()

    override val mainLayout: View by lazy {
        binding.constraintMainLogin
    }

    override fun setObserve()
    {
        super.setObserve()
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

            Toast.makeText(this@LoginView,message, Toast.LENGTH_SHORT).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        }


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