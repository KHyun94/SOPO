package com.delivery.sopo.presentation.views.signup

import android.content.Intent
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.SignUpViewBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.moveToActivityWithFinish
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.presentation.viewmodels.signup.SignUpViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpView: BaseView<SignUpViewBinding, SignUpViewModel>()
{
    override val layoutRes: Int = R.layout.sign_up_view
    override val vm: SignUpViewModel by viewModel()
    override val mainLayout by lazy { binding.constraintMainSignUp }

    override fun setObserve()
    {
        super.setObserve()

        vm.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this@SignUpView, focus)
            vm.validity[res.first] = res.second
        })

        vm.invalidity.observe(this, Observer { target ->
            val message = when(target.first)
            {
                InfoEnum.EMAIL ->
                {
                    binding.etEmail.requestFocus()
                    "이메일을 확인해주세요."
                }
                InfoEnum.PASSWORD ->
                {
                    binding.etPassword.requestFocus()
                    "비밀번호 확인해주세요."
                }
                InfoEnum.RE_PASSWORD ->
                {
                    binding.etRePassword.requestFocus()
                    "비밀번호 확인을 확인해주세요."
                }
                InfoEnum.AGREEMENT ->
                {
                    "약관을 확인해주세요."
                }
                else -> throw Exception("비정상 형식 에러 발생")
            }

            CustomSnackBar(binding.constraintMainSignUp, message, 3000).show()
        })

        vm.navigator.observe(this) { navigator ->
            when(navigator)
            {
                NavigatorConst.TO_COMPLETE ->
                {
                    moveToActivityWithFinish(SignUpCompleteView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
        }
    }

}