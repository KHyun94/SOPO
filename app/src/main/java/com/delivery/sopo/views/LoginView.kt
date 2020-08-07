package com.delivery.sopo.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.delivery.sopo.GeneralDialog
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.firebase.FirebaseUserManagement
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.models.APIResult
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
            this.validateResult?.observe(this@LoginView, Observer {

                if (it.result)
                {
                    if(it.data != null){
                        // 모든 유효성 검사 통과
                        startActivity(Intent(this@LoginView, MainView::class.java))
                        finish()
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
                                var msg : String? = null

                                if(it.data != 1){
                                    msg = resources.getString(it.data as Int)
                                } else {
                                    msg = it.msg
                                }
                                GeneralDialog(
                                    act = parentActivity,
                                    title = "오류",
                                    msg = msg,
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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

}