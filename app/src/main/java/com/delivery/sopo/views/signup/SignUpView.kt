package com.delivery.sopo.views.signup

import android.content.Intent
import android.view.Gravity
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.databinding.SignUpViewBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.signup.SignUpViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpView: BasicView<SignUpViewBinding>(R.layout.sign_up_view)
{
    private val vm: SignUpViewModel by viewModel()
    private var progressBar: CustomProgressBar? = CustomProgressBar(this@SignUpView)

    init
    {
        parentActivity = this@SignUpView
    }

    override fun bindView()
    {
        binding.vm = vm
    }

    override fun setObserver()
    {
        binding.vm!!.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this@SignUpView, focus)
            binding.vm!!.validities[res.first] = res.second
        })

        binding.vm!!.invalid.observe(this, Observer { target ->
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
                InfoEnum.RE_PASSWORD ->
                {
                    binding.etRePassword.requestFocus()
                    "비밀번호 확인을 확인해주세요."
                }
                InfoEnum.AGREEMENT ->
                {
                    "약관을 확인해주세요."
                }
                else -> throw Exception("비정상 형식 에러 발생")
            }

            Toast.makeText(this@SignUpView,message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        })

        binding.vm!!.isProgress.observe(this, Observer {isProgress ->
            if(progressBar == null)
            {
                progressBar = CustomProgressBar(this@SignUpView)
            }

            progressBar?.onStartProgress(isProgress){isDismiss ->
                if(isDismiss) progressBar = null
            }
        })

        binding.vm!!.result.observe(this, Observer { result ->

            if (!result.result)
            {
                GeneralDialog(act = parentActivity, title = "오류", msg = result.message, detailMsg = result.code?.CODE, rHandler = Pair(first = "네", second = null)).show(supportFragmentManager, "tag")
                return@Observer
            }

            GeneralDialog(parentActivity, "알림", "정상적으로 회원가입 성공했습니다.", null, Pair("네", { it ->
                    it.dismiss()
                    Intent(this@SignUpView, SignUpCompleteView::class.java).launchActivity(this)
                    finish()
                })).show(supportFragmentManager.beginTransaction(), "TAG")
        })
    }
}