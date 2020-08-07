package com.delivery.sopo.views

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delivery.sopo.GeneralDialog
import com.delivery.sopo.R
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.databinding.SignUpViewBinding
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.models.ValidateResult
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.viewmodels.SignUpViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpView : BasicView<SignUpViewBinding>(R.layout.sign_up_view)
{

    private val signUpVM: SignUpViewModel by viewModel()

    init
    {
        TAG += this.javaClass.simpleName
        parentActivity = this@SignUpView
    }

    override fun bindView()
    {
        binding.vm = signUpVM
    }

    override fun setObserver()
    {
        binding.vm?.run {
            this.validateResult?.observe(this@SignUpView, Observer {

                if (it.result)
                {
                    if(it.data != null){
                        // 모든 유효성 검사 통과
                        GeneralDialog(
                            parentActivity,
                            "알림",
                            "회원가입에 성공하셨습니다.\n해당 이메일에 인증메일을 확인해주세요.",
                            null,
                            Pair("네", { it ->
                                it.dismiss()
                                startActivity(
                                    Intent(
                                        this@SignUpView,
                                        SignUpClear::class.java
                                    )
                                )
                                finish()
                            })
                        ).show(
                            supportFragmentManager.beginTransaction(),
                            "TAG"
                        )
                    }


                    return@Observer
                }
                else
                {   // 유효성 검사 실패
                    when (it.showType)
                    {
                        InfoConst.NON_SHOW ->
                        {
                            return@Observer
                        }
                        InfoConst.CUSTOM_TOAST_MSG ->
                        {
                            Log.d(TAG, "토스트 메시지 ${it.msg}")
                            CustomAlertMsg.floatingUpperSnackBAr(
                                context = parentActivity,
                                msg = it.msg,
                                isClick = true
                            )

                            return@Observer
                        }
                        InfoConst.CUSTOM_DIALOG ->
                        {
                            if (it.result && it.data != null)
                            {

                                return@Observer
                            }
                            else
                            {
                                GeneralDialog(
                                    act = parentActivity,
                                    title = "오류",
                                    msg = resources.getString(it.data as Int),
                                    detailMsg = null,
                                    rHandler = Pair(
                                        first = "확인",
                                        second = { it ->
                                            it.dismiss()
                                        })
                                ).show(supportFragmentManager, "tag")

                                return@Observer
                            }

                        }
                        InfoConst.ERROR_ACTIVITY ->
                        {
                            return@Observer
                        }
                    }

                }
            })

        }
    }
}