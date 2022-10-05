package com.delivery.sopo.presentation.views.signup

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterNicknameViewBinding
import com.delivery.sopo.extensions.convertTextColor
import com.delivery.sopo.extensions.moveActivity
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.presentation.viewmodels.signup.RegisterNicknameViewModel
import com.delivery.sopo.presentation.views.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import org.koin.androidx.viewmodel.ext.android.viewModel

@AndroidEntryPoint
class RegisterNicknameView: BaseView<RegisterNicknameViewBinding, RegisterNicknameViewModel>()
{
    override val vm: RegisterNicknameViewModel by viewModels()
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
                    moveActivity(MainActivity::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK){
                        finish()
                    }
                }
            }


        }
    }


}