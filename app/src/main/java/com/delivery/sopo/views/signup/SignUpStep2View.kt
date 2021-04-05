package com.delivery.sopo.views.signup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.databinding.SignUpStep2ViewBinding
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.viewmodels.signup.SignUpStep2ViewModel
import com.delivery.sopo.views.widget.CustomEditText
import org.koin.android.ext.android.inject
import java.util.regex.Pattern

class SignUpStep2View: AppCompatActivity()
{
    private lateinit var binding: SignUpStep2ViewBinding
    private val vm: SignUpStep2ViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<SignUpStep2ViewBinding>(this, R.layout.sign_up_step2_view)
        binding.vm = vm
        binding.lifecycleOwner = this

        setObserve()
    }

    private fun setObserve()
    {
        binding.vm!!.nickname.observe(this@SignUpStep2View, Observer { nickname ->

            if(nickname.isEmpty())
            {
                binding.vm!!.setVisibleState(type = InfoConst.NICKNAME, errorState = View.GONE, corState = View.GONE)
                binding.vm!!.statusType.value = CustomEditText.STATUS_COLOR_ELSE
                return@Observer
            }

            if(ValidateUtil.isValidateNickname(nickname))
            {
                binding.vm!!.setVisibleState(type = InfoConst.NICKNAME, errorState = View.GONE, corState = View.VISIBLE)
                binding.vm!!.statusType.value = CustomEditText.STATUS_COLOR_BLUE

                return@Observer
            }

            binding.vm!!.setVisibleState(type = InfoConst.NICKNAME, errorState = View.VISIBLE, corState = View.GONE)
            binding.vm!!.statusType.value = CustomEditText.STATUS_COLOR_RED

        })
    }
}