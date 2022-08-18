package com.delivery.sopo.presentation.views.signup

import android.content.Intent
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterNicknameViewBinding
import com.delivery.sopo.extensions.convertTextColor
import com.delivery.sopo.extensions.moveToActivityWithFinish
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.presentation.viewmodels.signup.RegisterNicknameViewModel
import com.delivery.sopo.presentation.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterNicknameView: BaseView<RegisterNicknameViewBinding, RegisterNicknameViewModel>()
{
    override val vm: RegisterNicknameViewModel by viewModel()
    override val layoutRes: Int = R.layout.register_nickname_view
    override val mainLayout: View by lazy { binding.constraintMainUpdateNickname }

    override fun onAfterBinding()
    {
        super.onAfterBinding()

        showKeyboard(binding.etInputText)
    }

    override fun setObserve()
    {
        super.setObserve()

        binding.etInputText.addTextChangedListener { nickname ->

            val isValidate = ValidateUtil.isValidateNickname(nickname.toString())

            if(isValidate)
            {
                binding.btnRegisterNickname.backgroundTintList = resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnRegisterNickname.convertTextColor(R.color.MAIN_WHITE)
            }
            else
            {
                binding.btnRegisterNickname.backgroundTintList = resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                binding.btnRegisterNickname.convertTextColor(R.color.COLOR_GRAY_400)
            }
        }

        vm.navigator.observe(this) {

            when(it)
            {
                NavigatorConst.Screen.MAIN ->
                {
                    /*val builder = SpannableStringBuilder("등록된 닉네임은\n${vm.nickname.value?.toString()}입니다.")
                    builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.COLOR_MAIN_700)), 8, builder.length - 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    val optionalClickListener: OnOptionalClickListener = { dialog ->
                        moveToActivityWithFinish(MainView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        dialog.dismiss()
                    }

                    val optionalDialog = OptionalDialog(optionalType = OptionalTypeEnum.ONE_WAY, title = builder, leftHandler = Pair("확인", optionalClickListener))

                    optionalDialog.show(supportFragmentManager, "")*/
                    moveToActivityWithFinish(MainView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            }


        }
    }


}