package com.delivery.sopo.views.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.databinding.ResetPasswordViewBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.TextInputUtil
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

        binding.layoutMainReset.setOnClickListener {
            Toast.makeText(this, "백그라운드 클릭", Toast.LENGTH_LONG).show()
            it.requestFocus()
            OtherUtil.hideKeyboardSoft(this)
        }
    }

    fun bindView()
    {
        binding = DataBindingUtil.setContentView(this@ResetPasswordView, R.layout.reset_password_view)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    fun setObserve()
    {
        binding.vm!!.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this, focus)
            binding.vm!!.validates[res.first] = res.second
        })

        binding.vm!!.validateError.observe(this, Observer { target ->

            if (target.second)
            {
//                binding.btnSndEmail.backgroundTintList =
//                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
//                binding.btnSndEmail.setTextColor(resources.getColor(R.color.MAIN_WHITE))
                return@Observer
            }

            val message = when (target.first)
            {
                InfoEnum.EMAIL ->
                {
//                    binding.btnSndEmail.backgroundTintList =
//                        resources.getColorStateList(R.color.COLOR_GRAY_200, null)
//                    binding.btnSndEmail.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))
//
//                    binding.etNickname.requestFocus()
                    "이메일 양식을 확인해주세요."
                }
                else -> ""
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        })

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

            val isValidate = ValidateUtil.isValidateEmail(email.toString())

            if (isValidate)
            {
                binding.btnSignUp.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnSignUp.setTextColor(resources.getColor(R.color.MAIN_WHITE))
            }
            else
            {
                binding.btnSignUp.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                binding.btnSignUp.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))

            }

        })
    }


}