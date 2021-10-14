package com.delivery.sopo.views.signup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.SignUpCompleteBinding
import com.delivery.sopo.extensions.launchActivityWithAllClear
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.signup.SignUpCompleteViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpCompleteView : BaseView<SignUpCompleteBinding, SignUpCompleteViewModel>()
{
    override val layoutRes: Int
        get() = R.layout.sign_up_complete
    override val vm: SignUpCompleteViewModel by viewModel()

    override fun setObserve()
    {
        vm.navigator.observe(this) { navigator ->

            when(navigator)
            {
                NavigatorConst.TO_MAIN ->
                {
                    Intent(this, MainView::class.java).launchActivityWithAllClear(this@SignUpCompleteView)
                }
                NavigatorConst.TO_UPDATE_NICKNAME ->
                {
                    Intent(this, RegisterNicknameView::class.java).launchActivityWithAllClear(this@SignUpCompleteView)
                }
                NavigatorConst.TO_LOGIN_SELECT ->
                {
                    Intent(this@SignUpCompleteView, MainView::class.java).launchActivityWithAllClear(this@SignUpCompleteView)
                }
            }

            finish()
        }
    }

}