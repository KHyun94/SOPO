package com.delivery.sopo.views.login

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.ResetPasswordConst
import com.delivery.sopo.databinding.ResetPasswordViewBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.convertTextColor
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.login.ResetPasswordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.set

class ResetPasswordView: BaseView<ResetPasswordViewBinding, ResetPasswordViewModel>()
{
    override val layoutRes: Int = R.layout.reset_password_view
    override val vm: ResetPasswordViewModel by viewModel()
    override val mainLayout: View by lazy { binding.layoutMainReset }

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
                binding.relativeMainAuthCode.background =
                    ContextCompat.getDrawable(this@ResetPasswordView, R.drawable.border_all_round_10dp_blue_scale)
                binding.etAuthCode.convertTextColor(R.color.COLOR_GRAY_800)
            }
            else
            {
                binding.relativeMainAuthCode.background =
                    ContextCompat.getDrawable(this@ResetPasswordView, R.drawable.border_all_round_10dp)
                binding.etAuthCode.convertTextColor(R.color.COLOR_GRAY_400)
            }
        }

        vm.navigator.observe(this) { navigator ->

            SopoLog.i("navigator Observe [data:${navigator}]")

            when(navigator)
            {
                ResetPasswordConst.INPUT_AUTH_CODE ->
                {
                    if(!isCountTimer) timer = countLimitTime()
                    timer?.start()
                }
                ResetPasswordConst.INPUT_PASSWORD_FOR_RESET ->
                {
                    timer?.cancel()
                    timer = null
                }
                NavigatorConst.TO_COMPLETE, NavigatorConst.TO_BACK_SCREEN ->
                {
                    finish()
                }
            }
        }
    }

    fun countLimitTime(): CountDownTimer
    {
        return object: CountDownTimer(30000, 1000)
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
                vm.postNavigator(ResetPasswordConst.INPUT_EMAIL_FOR_RESEND)
                vm.authCode.postValue("")

                binding.tvCountOfAuth.text = "인증시간 초과"

                Handler(Looper.getMainLooper()).postDelayed({
                    binding.etEmail.requestFocus()
                }, 100)

                isCountTimer = false
            }

        }
    }
}