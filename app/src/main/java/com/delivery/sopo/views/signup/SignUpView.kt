package com.delivery.sopo.views.signup

import android.content.Intent
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.databinding.SignUpViewBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.signup.SignUpViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.login.LoginView
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

                when (result.displayType)
                {
                    DisplayEnum.TOAST_MESSAGE -> CustomAlertMsg.floatingUpperSnackBAr(context = this@SignUpView, msg = result.message, isClick = true)
                    DisplayEnum.DIALOG ->
                    {
                        GeneralDialog(
                            act = parentActivity, title = "오류", msg = result.message, detailMsg = result.code?.CODE, rHandler = Pair(first = "네", second = null)
                        ).show(supportFragmentManager, "tag")
                    }
                    else -> return@Observer
                }

                return@Observer
            }

            GeneralDialog(
                parentActivity, "알림", "정상적으로 회원가입 성공했습니다.", null, Pair("네", { it ->
                    it.dismiss()
                    Intent(this@SignUpView, LoginView::class.java).launchActivity(this)
                    finish()
                })
            ).show(supportFragmentManager.beginTransaction(), "TAG")

            return@Observer

        })
    }
}