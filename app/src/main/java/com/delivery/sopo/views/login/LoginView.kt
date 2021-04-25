package com.delivery.sopo.views.login

import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.LoginViewBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.login.LoginViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.UpdateNicknameView
import com.google.android.material.textfield.TextInputLayout
import okhttp3.internal.notifyAll
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginView: BasicView<LoginViewBinding>(R.layout.login_view)
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
        binding.vm!!.focus.observe(this, Observer { focus ->

            if (!focus.second)
            {
                focusOut(focus.third)
                binding.layoutEmail.refreshEndIconDrawableState()
                return@Observer
            }

            focusIn(focus.third)
        })

        binding.vm!!.isProgress.observe(this, Observer { isProgress ->
            if (isProgress == null) return@Observer

            if (progressBar == null)
            {
                progressBar = CustomProgressBar(this)
            }

            progressBar?.onStartProgress(isProgress) { isDismiss ->
                if (isDismiss) progressBar = null
            }
        })

        binding.vm!!.result.observe(this, Observer { result ->

            SopoLog.d(
                """
                Self Login Result >>> ${result.message}                
                ${result.result}
                ${result.code}
                ${result.data}
                ${result.displayType}

            """.trimIndent()
            )

            if (!result.result)
            {
                when (result.displayType)
                {
                    DisplayEnum.TOAST_MESSAGE ->
                    {
                        CustomAlertMsg.floatingUpperSnackBAr(context = this@LoginView, msg = result.message, isClick = true)
                    }
                    DisplayEnum.DIALOG ->
                    {
                        GeneralDialog(
                            act = this@LoginView, title = "오류", msg = result.message, detailMsg = result.code?.CODE, rHandler = Pair(first = "네", second = null)
                        ).show(supportFragmentManager, "tag")
                    }
                    else -> return@Observer
                }

                return@Observer
            }

            val userDetail = result.data as UserDetail

            if (userDetail.nickname == "")
            {
                startActivity(Intent(this@LoginView, UpdateNicknameView::class.java))
                return@Observer
            }

            Toast.makeText(this, "성공", Toast.LENGTH_LONG).show()
            startActivity(Intent(this@LoginView, MainView::class.java))
            finish()
        })

        binding.vm!!.navigator.observe(this@LoginView, Observer { navigator ->
            when (navigator)
            {
                NavigatorConst.TO_RESET_PASSWORD ->
                {
                    Intent(this@LoginView, ResetPasswordView::class.java).launchActivity(this@LoginView)
                }
            }
        })
    }

    fun focusIn(type: String)
    {
        SopoLog.d("${type}::focus in")

        /**
         * progress,
         * BoxBackground -> GRAY_50
         * ErrorMessage off
         * endIcon -> clearMark
         *         -> clearEvent
         */
        when (type)
        {
            InfoConst.EMAIL ->
            {

                binding.layoutEmail.isHintEnabled = true
                binding.layoutEmail.boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_50)
                binding.layoutEmail.boxStrokeWidth = SizeUtil.changeDpToPx(this, 2.0f)
                binding.layoutEmail.boxStrokeColor = resources.getColor(R.color.COLOR_MAIN_700)
                binding.layoutEmail.isEndIconVisible = true
                binding.layoutEmail.endIconDrawable = resources.getDrawable(R.drawable.ic_textinput_status_clear)
                binding.layoutEmail.helperText = null
            }
            InfoConst.PASSWORD ->
            {

            }
        }
    }

    fun focusOut(type: String)
    {

        SopoLog.d("${type}::focus out")

        when (type)
        {
            InfoConst.EMAIL ->
            {
                if(binding.vm!!.email.value.toString() == "")
                {
                    binding.layoutEmail.run {
                        isHintEnabled = true
                        boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_100)

                        boxStrokeWidth = SizeUtil.changeDpToPx(this@LoginView, 0.0f)
                        boxStrokeColor = ContextCompat.getColor(this@LoginView, R.color.COLOR_MAIN_700)

                        isEndIconVisible = true
                        endIconMode = TextInputLayout.END_ICON_CUSTOM
                        helperText = "이메일 양식을 확인해주세요."
                        endIconDrawable = ContextCompat.getDrawable(this@LoginView, R.drawable.ic_textinput_status_fail)
                    }
                    return
                }

                if (!ValidateUtil.isValidateEmail(binding.vm!!.email.value.toString()))
                {
                    SopoLog.d("이메일 양식 확인 실패 >>>${binding.vm!!.email.value.toString()}")

                    binding.layoutEmail.run {
                        isHintEnabled = true
                        boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_50)

                        boxStrokeWidth = SizeUtil.changeDpToPx(this@LoginView, 2.0f)
                        boxStrokeColor = ContextCompat.getColor(this@LoginView, R.color.COLOR_MAIN_700)
                        isEndIconVisible = true
                        endIconMode = TextInputLayout.END_ICON_CUSTOM
                        helperText = "이메일 양식을 확인해주세요."
                        endIconDrawable = ContextCompat.getDrawable(this@LoginView, R.drawable.ic_textinput_status_fail)
                    }
                    return
                }
                SopoLog.d("이메일 양식 확인 상 >>>${binding.vm!!.email.value.toString()}")
                binding.layoutEmail.run {
                    boxBackgroundColor = resources.getColor(R.color.COLOR_MAIN_BLUE_50)
                    isEndIconVisible = true
                    endIconMode = TextInputLayout.END_ICON_CUSTOM
                    endIconDrawable = ContextCompat.getDrawable(this@LoginView, R.drawable.ic_textinput_status_success)
                }


                /**
                 * if validate is succeed,
                 * BoxBackground -> MAIN_BLUE_50
                 * ErrorMessage off
                 * endIcon -> errorMark
                 * endIcon -> successMark
                 */
            }
            InfoConst.PASSWORD ->
            {

            }
        }

    }
}