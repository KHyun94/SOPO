package com.delivery.sopo.presentation.login

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import com.delivery.sopo.R
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.extensions.moveActivity
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.presentation.views.login.ResetPasswordView
import com.delivery.sopo.presentation.views.main.MainActivity
import com.delivery.sopo.util.ui_util.TextInputUtil
import dagger.hilt.android.AndroidEntryPoint
import org.koin.androidx.viewmodel.ext.android.viewModel

@AndroidEntryPoint
class LoginView: BaseView<LoginViewBinding, LoginViewModel>()
{
    override val layoutRes: Int = R.layout.login_view
    override val vm: LoginViewModel by viewModels()
    override val mainLayout: View by lazy { binding.constraintMainLogin }

    override fun setObserve()
    {
        super.setObserve()

        vm.focus.observe(this) { focus ->
            TextInputUtil.changeFocusWithoutValidation(this@LoginView, focus)
        }

        vm.navigator.observe(this@LoginView) { navigator ->
            when(navigator)
            {
                NavigatorConst.Screen.RESET_PASSWORD ->
                {
                    moveActivity(ResetPasswordView::class.java)
                }
                NavigatorConst.Screen.UPDATE_NICKNAME ->
                {
                    moveActivity(ResetPasswordView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK) {
                        finish()
                    }
                }
                NavigatorConst.Screen.MAIN ->
                {
                    moveActivity(MainActivity::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK) {
                        finish()
                    }
                }
            }
        }
    }
}