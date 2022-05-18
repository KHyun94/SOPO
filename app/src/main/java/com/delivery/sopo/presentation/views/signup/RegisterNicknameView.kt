package com.delivery.sopo.presentation.views.signup

import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterNicknameViewBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.enums.OptionalTypeEnum
import com.delivery.sopo.extensions.convertTextColor
import com.delivery.sopo.extensions.moveToActivityWithFinish
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.presentation.viewmodels.signup.RegisterNicknameViewModel
import com.delivery.sopo.presentation.views.dialog.OptionalDialog
import com.delivery.sopo.presentation.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterNicknameView: BaseView<RegisterNicknameViewBinding, RegisterNicknameViewModel>()
{
    override val vm: RegisterNicknameViewModel by viewModel()
    override val layoutRes: Int = R.layout.register_nickname_view
    override val mainLayout: View by lazy { binding.constraintMainUpdateNickname }

    override fun setObserve()
    {
        super.setObserve()

        binding.etNickname.addTextChangedListener { nickname ->

            val isValidate = ValidateUtil.isValidateNickname(nickname.toString())

            if(isValidate)
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnSndEmail.convertTextColor(R.color.MAIN_WHITE)
            }
            else
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                binding.btnSndEmail.convertTextColor(R.color.COLOR_GRAY_400)

            }
        }

        vm.focus.observe(this) { focus ->
            val res = TextInputUtil.changeFocus(this@RegisterNicknameView, focus)
            vm.validates[res.first] = res.second
        }

        vm.validateError.observe(this, Observer { target ->

            if(target.second)
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                val colorRes = ContextCompat.getColor(this, R.color.MAIN_WHITE)
                binding.btnSndEmail.setTextColor(colorRes)
                return@Observer
            }

            val message = when(target.first)
            {
                InfoEnum.NICKNAME ->
                {
                    binding.btnSndEmail.backgroundTintList =
                        resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                    binding.btnSndEmail.convertTextColor(R.color.COLOR_GRAY_400)

                    binding.etNickname.requestFocus()
                    "닉네임을 확인해주세요."
                }
                else -> ""
            }

            Toast.makeText(this@RegisterNicknameView, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        })


        vm.navigator.observe(this) {

            when(it)
            {
                NavigatorEnum.MAIN ->
                {
                    val builder =
                        SpannableStringBuilder("등록된 닉네임은\n${vm.nickname.value?.toString()}입니다.")
                    builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.COLOR_MAIN_700)), 8, builder.length - 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    val optionalDialog =
                        OptionalDialog(optionalType = OptionalTypeEnum.ONE_WAY, title = builder, leftHandler = Pair("확인") {
                            moveToActivityWithFinish(MainView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        })

                    optionalDialog.show(supportFragmentManager, "")
                }
            }


        }
    }


}