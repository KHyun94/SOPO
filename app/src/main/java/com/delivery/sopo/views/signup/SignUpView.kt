package com.delivery.sopo.views.signup

import android.content.Intent
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.databinding.SignUpViewBinding
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.viewmodels.signup.SignUpViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpView: BasicView<SignUpViewBinding>(R.layout.sign_up_view)
{
    private val vm: SignUpViewModel by viewModel()

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
        binding.vm!!.result.observe(this, Observer {
            if (it == null) return@Observer

            if (it.errorResult != null)
            {
                when (it.errorResult!!.errorType)
                {
                    ErrorResult.ERROR_TYPE_NON -> return@Observer
                    ErrorResult.ERROR_TYPE_TOAST ->
                    {
                        CustomAlertMsg.floatingUpperSnackBAr(
                            context = parentActivity, msg = it.errorResult!!.errorMsg, isClick = true
                        )
                        return@Observer
                    }
                    ErrorResult.ERROR_TYPE_DIALOG ->
                    {
                        val code = it.errorResult!!.code?.CODE
                        val msg = it.errorResult!!.errorMsg

                        GeneralDialog(
                            act = parentActivity, title = "오류", msg = msg, detailMsg = code, rHandler = Pair(first = "네", second = null)
                        ).show(supportFragmentManager, "tag")
                    }
                    ErrorResult.ERROR_TYPE_SCREEN -> return@Observer
                    else -> return@Observer
                }
            }

            if (it.successResult != null)
            {
                SopoLog.d(msg = "성공 발생 => ${it.successResult}")

                val successResult = it.successResult

                GeneralDialog(parentActivity, "알림", successResult?.successMsg?:"정상적으로 회원가입 성공했습니다.\n다음 스텝을 진행해주세요.", null, Pair("네", { it ->
                        it.dismiss()
                        Intent(this, SignUpStep2View::class.java).launchActivity(this)
                        finish()
                    })
                ).show(supportFragmentManager.beginTransaction(), "TAG")

                return@Observer
            }


        })
    }
}