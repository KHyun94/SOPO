package com.delivery.sopo.views

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delivery.sopo.util.ui_util.GeneralDialog
import com.delivery.sopo.R
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.viewmodels.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginView : BasicView<LoginViewBinding>(R.layout.login_view)
{

    private val loginVM: LoginViewModel by viewModel()

    init
    {
        TAG += this.javaClass.simpleName
        parentActivity = this@LoginView
    }

    override fun bindView()
    {
        binding.vm = loginVM
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
        binding.vm?.run {
            this.validateResult.observe(this@LoginView, Observer { res ->

                if (res.result)
                {
                    if (res.data != null)
                    {
                        // 모든 유효성 검사 통과
                        startActivity(Intent(this@LoginView, MainView::class.java))
                        finish()
                    }

                }
                else
                {
                    when (res.showType)
                    {
                        InfoConst.NON_SHOW ->
                        {
                            return@Observer
                        }
                        InfoConst.CUSTOM_TOAST_MSG ->
                        {
                            CustomAlertMsg.floatingUpperSnackBAr(
                                context = parentActivity,
                                msg = res.msg,
                                isClick = true
                            )

                            return@Observer
                        }
                        InfoConst.CUSTOM_DIALOG ->
                        {
                            if (res.result && res.data != null)
                            {
                                return@Observer
                            }
                            else
                            {
                                when (res.data)
                                {
                                    null ->
                                    {
                                        GeneralDialog(
                                            act = parentActivity,
                                            title = "오류",
                                            msg = res.msg,
                                            detailMsg = null,
                                            rHandler = Pair(
                                                first = "네",
                                                second = { it -> it.dismiss() })
                                        ).show(supportFragmentManager, "tag")
                                    }
                                    is String ->
                                    {
                                        val token = res.data as String

                                        GeneralDialog(
                                            act = parentActivity,
                                            title = "오류",
                                            msg = res.msg,
                                            detailMsg = null,
                                            rHandler = Pair(
                                                first = "네",
                                                second = { it ->
                                                    this.authJwtToken(jwtToken = token)
                                                    it.dismiss()
                                                }),
                                            lHandler = Pair(
                                                first = "아니오",
                                                second = { it -> it.dismiss() })
                                        ).show(supportFragmentManager, "tag")
                                    }
                                    else ->
                                    {
                                        GeneralDialog(
                                            act = parentActivity,
                                            title = "오류",
                                            msg = "알 수 없는 오류",
                                            detailMsg = null,
                                            rHandler = Pair(
                                                first = "확인",
                                                second = { it ->
                                                    it.dismiss()
                                                })
                                        ).show(supportFragmentManager, "tag")
                                    }
                                }

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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

}