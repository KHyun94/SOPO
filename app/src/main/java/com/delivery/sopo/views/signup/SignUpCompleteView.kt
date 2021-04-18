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
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.signup.SignUpCompleteViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.login.LoginSelectView
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpCompleteView : AppCompatActivity()
{
    lateinit var binding: SignUpCompleteBinding
    private val vm: SignUpCompleteViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        bindView()
        setObserve()
    }

    private fun bindView()
    {
        binding = DataBindingUtil.setContentView(this@SignUpCompleteView, R.layout.sign_up_complete)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    private fun setObserve()
    {
        binding.vm!!.navigator.observe(this, Observer { navigator ->

            when(navigator)
            {
                NavigatorConst.TO_MAIN ->
                {
                    Intent(this, MainView::class.java).launchActivityWithAllClear(this@SignUpCompleteView)
                }
                NavigatorConst.TO_UPDATE_NICKNAME ->
                {
                    Intent(this, MainView::class.java).launchActivityWithAllClear(this@SignUpCompleteView)
                }
                NavigatorConst.TO_LOGIN_SELECT ->
                {
                    Intent(this@SignUpCompleteView, MainView::class.java).launchActivityWithAllClear(this@SignUpCompleteView)
                }
            }

            finish()
        })

        binding.vm!!.result.observe(this, Observer {res ->
            if(!res.result)
            {
                SopoLog.e("${res.message} >>> ${res.code}")
                GeneralDialog(this@SignUpCompleteView, "오류", res.message, res.code?.CODE, Pair("네", object: OnAgreeClickListener{
                    override fun invoke(agree: GeneralDialog)
                    {
                        binding.vm!!.navigator.postValue(NavigatorConst.TO_LOGIN_SELECT)
                    }
                }))
            }

        })
    }
}