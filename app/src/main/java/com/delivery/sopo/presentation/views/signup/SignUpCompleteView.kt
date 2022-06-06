package com.delivery.sopo.presentation.views.signup

import android.content.Intent
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.SignUpCompleteBinding
import com.delivery.sopo.extensions.moveToActivityWithFinish
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.viewmodels.signup.SignUpCompleteViewModel
import com.delivery.sopo.presentation.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpCompleteView : BaseView<SignUpCompleteBinding, SignUpCompleteViewModel>()
{
    override val layoutRes: Int = R.layout.sign_up_complete
    override val vm: SignUpCompleteViewModel by viewModel()
    override val mainLayout by lazy { binding.constraintMainSignUpComplete }

    override fun setObserve()
    {
        super.setObserve()

        vm.navigator.observe(this) { navigator ->

            val toActivity = when(navigator)
            {
                NavigatorConst.Screen.MAIN -> MainView::class.java
                NavigatorConst.Screen.UPDATE_NICKNAME -> RegisterNicknameView::class.java
                else -> throw Exception("정상적인 접근이 아닙니다.")
            }

            moveToActivityWithFinish(toActivity, Intent.FLAG_ACTIVITY_CLEAR_TASK)
            finish()
        }
    }

}