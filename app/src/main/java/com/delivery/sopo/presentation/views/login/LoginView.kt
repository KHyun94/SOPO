package com.delivery.sopo.presentation.views.login

import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.extensions.moveToActivity
import com.delivery.sopo.extensions.moveToActivityWithFinish
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.presentation.viewmodels.login.LoginViewModel
import com.delivery.sopo.presentation.views.main.MainView
import com.delivery.sopo.presentation.views.signup.RegisterNicknameView
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginView: BaseView<LoginViewBinding, LoginViewModel>()
{
    override val layoutRes: Int = R.layout.login_view
    override val vm: LoginViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainLogin }

    override fun setObserve()
    {
        super.setObserve()

        vm.focus.observe(this) { focus ->
            TextInputUtil.changeFocusWithoutValidation(this@LoginView, focus)
//            vm.validity[res.first] = res.second
        }

        vm.navigator.observe(this@LoginView) { navigator ->
            when(navigator)
            {
                NavigatorConst.Screen.RESET_PASSWORD ->
                {
                    moveToActivity(ResetPasswordView::class.java)
                }
                NavigatorConst.Screen.UPDATE_NICKNAME ->
                {
                    moveToActivityWithFinish(RegisterNicknameView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    finish()
                }
                NavigatorConst.Screen.MAIN ->
                {
                    moveToActivityWithFinish(MainView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    finish()
                }
            }
        }
    }
}