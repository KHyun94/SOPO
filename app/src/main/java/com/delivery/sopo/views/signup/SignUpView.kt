package com.delivery.sopo.views.signup

import android.content.Intent
import android.view.Gravity
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.SignUpViewBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.signup.SignUpViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpView: BaseView<SignUpViewBinding, SignUpViewModel>()
{

    override val layoutRes: Int
        get() = R.layout.sign_up_view
    override val vm: SignUpViewModel by viewModel()

    override fun setObserve()
    {
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

            Toast.makeText(this@SignUpView, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        })

        vm.error.observe(this) {

            OtherUtil.hideKeyboardSoft(this)

            when(it.first)
            {
                500 ->
                {
                    CustomSnackBar.make(view = binding.constraintSighUp, content = it.second, duration = 3000, type = SnackBarEnum.ERROR).show()
                }
                else ->
                {
                    CustomSnackBar.make(view = binding.constraintSighUp, content = it.second, duration = 3000, type = SnackBarEnum.ERROR).show()
                }
            }
        }

        vm.navigator.observe(this) { navigator ->
            when(navigator)
            {
                NavigatorConst.TO_COMPLETE ->
                {
                    GeneralDialog(this, "알림", "정상적으로 회원가입 성공했습니다.", null, Pair("네", { it ->
                        it.dismiss()
                        Intent(this@SignUpView, SignUpCompleteView::class.java).launchActivity(this)
                        finish()
                    })).show(supportFragmentManager.beginTransaction(), "TAG")
                }
            }
        }
    }

}