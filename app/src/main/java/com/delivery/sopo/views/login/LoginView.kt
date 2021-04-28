package com.delivery.sopo.views.login

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
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
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.login.LoginViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.UpdateNicknameView
import com.google.android.material.textfield.TextInputEditText
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
        binding.vm!!.focus.observe(this, Observer { focus -> TextInputUtil.changeFocus(this@LoginView, focus) })

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
                            act = this@LoginView, title = "오류", msg = result.message, detailMsg = result.code?.CODE, rHandler = Pair(first = "네", second = object: OnAgreeClickListener{
                                override fun invoke(agree: GeneralDialog)
                                {
                                    agree.dismiss()
                                }

                            })
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

//    private fun focusIn(context: Context, focus: Triple<View, Boolean, InfoEnum>)
//    {
//        SopoLog.d("${focus.third.NAME}::focus in")
//
//        val textInputEditText = (focus.first as TextInputEditText)
//        val textInputLayout = textInputEditText.parent.parent as TextInputLayout
//
//        val hasFocus = focus.second
//        val infoEnum = focus.third
//
//        textInputLayout.run {
//            // 힌트 홣성화
//            isHintEnabled = true
//            hint = focus.third.NAME
//            // 내부 이너 박스 컬러 >>> GRAY_50
//            boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_50)
//            // endIcon >>> Visible, clear img
//
//            error = null
//            errorIconDrawable = null
//
//            isEndIconVisible = true
//            endIconMode = TextInputLayout.END_ICON_CUSTOM
//            endIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_clear)
//
//            setEndIconOnClickListener {
//                editText?.setText("")
//            }
//        }
//    }
//
//    fun focusOut(context: Context, focus: Triple<View, Boolean, InfoEnum>)
//    {
//        SopoLog.d("${focus.third.NAME}::focus out")
//
//        val textInputEditText = (focus.first as TextInputEditText)
//        val textInputLayout = textInputEditText.parent.parent as TextInputLayout
//
//        val hasFocus = focus.second
//        val infoEnum = focus.third
//
//        textInputLayout.setEndIconOnClickListener(null)
//
//        var errorMessage = ""
//        var isValidate = false
//
//        when (infoEnum)
//        {
//            InfoEnum.EMAIL ->
//            {
//                isValidate = ValidateUtil.isValidateEmail(textInputEditText.text.toString())
//
//                if(!isValidate)
//                {
//                    errorMessage = "이메일 양식을 확인해주세요."
//                }
//            }
//            InfoEnum.PASSWORD ->
//            {
//                isValidate = ValidateUtil.isValidatePassword(textInputEditText.text.toString())
//
//                if(!isValidate)
//                {
//                    errorMessage = "비밀번호 형식을 확인해주세요."
//                }
//            }
//            InfoEnum.NICKNAME ->
//            {
//                isValidate = ValidateUtil.isValidateNickname(textInputEditText.text.toString())
//
//                if(!isValidate)
//                {
//                    errorMessage = "닉네임 형식을 확인해주세요."
//                }
//            }
//            else ->
//            {
//                isValidate = false
//            }
//        }
//
//        if(textInputEditText.text.toString().isEmpty())
//        {
//            textInputLayout.run {
//                isHintEnabled = true
//                hint = infoEnum.NAME
//                boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_100)
//
//                boxStrokeWidth = SizeUtil.changeDpToPx(context, 0.0f)
//                boxStrokeColor = ContextCompat.getColor(context, R.color.COLOR_MAIN_700)
//
//                error = null
//
//                endIconDrawable = null
////                isErrorEnabled = true
//                errorIconDrawable = ContextCompat.getDrawable(context, R.color.COLOR_GRAY_100)
//            }
//            return
//        }
//
//        if (!isValidate)
//        {
//            SopoLog.d("${infoEnum.NAME}'s validation is failed >>>${textInputEditText.text.toString()}")
//
//            textInputLayout.run {
//                isHintEnabled = true
//                hint = infoEnum.NAME
//                boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_50)
//
//                boxStrokeWidth = SizeUtil.changeDpToPx(context, 2.0f)
//                boxStrokeColor = ContextCompat.getColor(context, R.color.COLOR_MAIN_700)
//
//                isEndIconVisible = false
//                endIconDrawable = null
//
//                error = errorMessage
//                errorIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_fail)
//            }
//            return
//        }
//
//        SopoLog.d("${focus.third.NAME}'s validation is success >>>${textInputEditText.text.toString()}")
//
//        textInputLayout.run {
//            isHintEnabled = false
//            hint = null
//            boxBackgroundColor = resources.getColor(R.color.COLOR_MAIN_BLUE_50)
//
//            boxStrokeWidth = SizeUtil.changeDpToPx(context, 0.0f)
//            boxStrokeErrorColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))
//
//            error = null
//            isErrorEnabled = true
//            errorIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_success)
//        }
//    }
}