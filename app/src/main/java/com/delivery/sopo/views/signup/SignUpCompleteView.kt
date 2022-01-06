package com.delivery.sopo.views.signup

import android.content.Intent
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.SignUpCompleteBinding
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.extensions.moveToActivity
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.viewmodels.signup.SignUpCompleteViewModel
import com.delivery.sopo.views.main.MainView
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
                NavigatorConst.TO_MAIN -> MainView::class.java
                NavigatorConst.TO_UPDATE_NICKNAME -> RegisterNicknameView::class.java
                else -> throw Exception("정상적인 접근이 아닙니다.")
            }

            moveToActivity(toActivity, Intent.FLAG_ACTIVITY_CLEAR_TASK)
            finish()
        }
    }

}