package com.delivery.sopo.presentation.views.login

import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ResetPasswordViewBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.OptionalTypeEnum
import com.delivery.sopo.extensions.convertBackground
import com.delivery.sopo.extensions.convertTextColor
import com.delivery.sopo.extensions.expanded
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.presentation.viewmodels.login.ResetPasswordViewModel
import com.delivery.sopo.presentation.views.dialog.OptionalDialog
import com.delivery.sopo.util.ui_util.TextInputUtil
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.set

class ResetPasswordView: BaseView<ResetPasswordViewBinding, ResetPasswordViewModel>()
{
    override val layoutRes: Int = R.layout.reset_password_view
    override val vm: ResetPasswordViewModel by viewModel()
    override val mainLayout: View by lazy { binding.slideMainResetPassword }

    var timer: CountDownTimer? = null
    private var isCountTimer = false

    override fun setObserve()
    {
        super.setObserve()

        vm.focus.observe(this) { focus ->
            val res = TextInputUtil.changeFocus(this, focus)
            vm.validity[res.first] = res.second
        }

        vm.focusOn.observe(this) { focusType ->

            when(focusType)
            {
                InfoEnum.EMAIL ->
                {
                    binding.etEmail.requestFocus()
                }
                InfoEnum.AUTH_CODE ->
                {
                    binding.etAuthCode.requestFocus()
                }
                InfoEnum.PASSWORD ->
                {
                    binding.etPassword.requestFocus()
                }
            }
        }

        vm.invalidity.observe(this, Observer { target ->

            if(target.second) return@Observer

            val message = when(target.first)
            {
                InfoEnum.EMAIL ->
                {
                    binding.etEmail.requestFocus()
                    "이메일 양식을 확인해주세요."
                }
                InfoEnum.PASSWORD ->
                {
                    binding.etPassword.requestFocus()
                    "비밀번호 확인해주세요."
                }
                else -> ""
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        })

        binding.etAuthCode.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus)
            {
                binding.relativeMainAuthCode.convertBackground(R.drawable.border_all_round_10dp_blue_scale)
                binding.etAuthCode.convertTextColor(R.color.COLOR_GRAY_800)
            }
            else
            {
                binding.relativeMainAuthCode.convertBackground(R.drawable.border_all_round_10dp)
                binding.etAuthCode.convertTextColor(R.color.COLOR_GRAY_400)
            }
        }

        vm.navigator.observe(this) { navigator ->
            when(navigator)
            {
                NavigatorConst.Error.INVALID_JWT_TOKEN ->
                {
                    binding.slideMainResetPassword.expanded()
                    binding.slideMainResetPassword.isTouchEnabled = false
                }
                NavigatorConst.Event.INPUT_AUTH_CODE ->
                {
                    if(!isCountTimer) timer = countLimitTime()
                    timer?.start()
                }
                NavigatorConst.Event.INPUT_PASSWORD_FOR_RESET ->
                {
                    timer?.cancel()
                    timer = null
                }
                NavigatorConst.Event.COMPLETE, NavigatorConst.Event.BACK ->
                {
                    finish()
                }
            }
        }
    }

    fun countLimitTime(): CountDownTimer
    {
        return object: CountDownTimer(180000, 1000)
        {
            override fun onTick(millisUntilFinished: Long)
            {
                isCountTimer = true

                val currentSecond = millisUntilFinished / 1000

                val currentMinutes = (currentSecond / 60)
                val currentSeconds = (currentSecond % 60)

                val time = if(currentMinutes > 0)
                {
                    "${currentMinutes}분 ${currentSeconds}초 내 입력"
                }
                else
                {
                    "${currentSeconds}초 내 입력"
                }

                binding.tvCountOfAuth.text = time
            }

            override fun onFinish()
            {
                vm.postNavigator(NavigatorConst.Event.INPUT_EMAIL_FOR_RESEND)
                vm.authCode.postValue("")

                binding.tvCountOfAuth.text = "인증시간 초과"

                val optionalDialog = OptionalDialog(optionalType = OptionalTypeEnum.ONE_WAY, title = "인증시간이 만료되었습니다", content= "이메일 인증을 다시 진행해주세요.", leftHandler = Pair("확인") { dialog ->
                    binding.etEmail.requestFocus()
                    isCountTimer = false
                    dialog.dismiss()
                })

                optionalDialog.show(supportFragmentManager, "")
            }

        }
    }
}