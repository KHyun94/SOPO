package com.delivery.sopo.views.login

import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.networks.handler.LoginHandler
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.login.LoginViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.UpdateNicknameView
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginView : BasicView<LoginViewBinding>(R.layout.login_view)
{
    private val loginVm: LoginViewModel by viewModel()
    private var progressBar: CustomProgressBar? = CustomProgressBar(this@LoginView)

    override fun bindView()
    {
        binding.vm = loginVm
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
        binding.vm!!.isProgress.observe(this, Observer { isProgress ->
            if(isProgress == null) return@Observer

            if(progressBar == null)
            {
                progressBar = CustomProgressBar(this)
            }

            progressBar?.onStartProgress(isProgress){isDismiss ->
                if(isDismiss) progressBar = null
            }
        })

        binding.vm!!.result.observe(this, Observer {result ->

            SopoLog.d("""
                Self Login Result >>> ${result.message}                
                ${result.result}
                ${result.code}
                ${result.data}
                ${result.displayType}

            """.trimIndent())

            if(!result.result)
            {
                when(result.displayType)
                {
                    DisplayEnum.TOAST_MESSAGE ->
                    {
                        CustomAlertMsg.floatingUpperSnackBAr(context = this@LoginView, msg = result.message, isClick = true)
                    }
                    DisplayEnum.DIALOG ->
                    {
                        GeneralDialog(
                            act = this@LoginView,
                            title = "오류",
                            msg = result.message,
                            detailMsg = result.code?.CODE,
                            rHandler = Pair(first = "네", second = null)
                        ).show(supportFragmentManager, "tag")
                    }
                    else -> return@Observer
                }

                return@Observer
            }

            val userDetail = result.data as UserDetail

            if(userDetail.nickname == "")
            {
                startActivity(Intent(this@LoginView, UpdateNicknameView::class.java))
                return@Observer
            }

            Toast.makeText(this, "성공", Toast.LENGTH_LONG).show()
            startActivity(Intent(this@LoginView, MainView::class.java))
            finish()
        })
    }
}