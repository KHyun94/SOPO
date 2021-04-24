package com.delivery.sopo.views.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.databinding.ResetPasswordViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.viewmodels.login.ResetPasswordViewModel
import com.delivery.sopo.views.menus.LockScreenView
import com.delivery.sopo.views.widget.CustomEditText
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResetPasswordView: AppCompatActivity()
{
    lateinit var binding: ResetPasswordViewBinding
    private val vm: ResetPasswordViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        bindView()
        setObserve()
    }

    fun bindView()
    {
        binding = DataBindingUtil.setContentView(this@ResetPasswordView, R.layout.reset_password_view)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    fun setObserve()
    {
        binding.vm!!.result.observe(this@ResetPasswordView, Observer {
            if(it.result)
            {
                startActivityForResult(
                    Intent(this@ResetPasswordView, LockScreenView::class.java).apply {
                        putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.RESET)
                    }, 11
                )
            }
        })

        binding.vm!!.email.observe(this@ResetPasswordView, Observer { email ->

            if (email.isEmpty())
            {
                binding.vm!!.setVisibleState(type = InfoConst.EMAIL, errorState = View.GONE, corState = View.GONE)
                binding.vm!!.statusType.value = CustomEditText.STATUS_COLOR_ELSE
                return@Observer
            }

            if (!ValidateUtil.isValidateEmail(email))
            {
                return@Observer
            }

            binding.vm!!.setVisibleState(type = InfoConst.EMAIL, errorState = View.GONE, corState = View.VISIBLE)
            binding.vm!!.statusType.value = CustomEditText.STATUS_COLOR_BLUE
        })
    }


}