package com.delivery.sopo.views.login

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.ResetPasswordViewBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.extensions.launchActivityForResult
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.login.ResetPasswordViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.menus.LockScreenView
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResetPasswordView: AppCompatActivity()
{
    lateinit var binding: ResetPasswordViewBinding
    private val vm: ResetPasswordViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = bindView(this@ResetPasswordView, R.layout.reset_password_view)
        setObserve()
    }

    fun <T : ViewDataBinding> bindView(activity: FragmentActivity, @LayoutRes layoutId : Int) : T
    {
        return DataBindingUtil.setContentView<T>(activity,layoutId).apply{
            lifecycleOwner = activity
            setVariable(BR.vm, vm)
            executePendingBindings()
        }
    }

    fun setObserve()
    {
        vm.resetType.observe(this, Observer {
            vm.validates.clear()

            when(it)
            {
                0 ->
                {
                    vm.validates[InfoEnum.EMAIL] = false
                }
                1 ->
                {
                    vm.validates[InfoEnum.PASSWORD] = false
                    updateUIForInputPassword()
                }
                2 ->
                {
                    updateUIForComplete()
                }
            }

            SopoLog.d("validates type >>> ${vm.validates.toString()}")

        })

        vm.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this, focus)
            vm.validates[res.first] = res.second
        })

        vm.validateError.observe(this, Observer { target ->

            if (target.second)
            {
                return@Observer
            }

            val message = when (target.first)
            {
                InfoEnum.EMAIL ->
                {
                    "이메일 양식을 확인해주세요."
                }
                else -> ""
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        })

        vm.result.observe(this@ResetPasswordView, Observer { result ->

            if (!result.result)
            {
                GeneralDialog(this, "오류", ResponseCode.UNKNOWN_ERROR.MSG, ResponseCode.UNKNOWN_ERROR.CODE, Pair("네", null)).show(supportFragmentManager, "DIALOG")
                return@Observer
            }

            when(vm.resetType.value)
            {
                0->
                {
                    if (result.data !is EmailAuthDTO)
                    {
                        GeneralDialog(this, "오류", "", result.code?.CODE, Pair("네", null)).show(supportFragmentManager, "DIALOG")
                        return@Observer
                    }

                    val data: EmailAuthDTO = result.data

                    Intent(this@ResetPasswordView, LockScreenView::class.java).apply {
                        putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.RESET)
                        putExtra("PIN_NUM", data.code)
                    }.launchActivityForResult(this@ResetPasswordView, 11)
                }
                1->
                {
                    vm.resetType.postValue(2)
                }
                2->
                {
                    updateUIForComplete()
                }
            }


        })

        vm.email.observe(this@ResetPasswordView, Observer { email ->

            val isValidate = ValidateUtil.isValidateEmail(email.toString())

            if (isValidate)
            {
                binding.btnNext.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnNext.setTextColor(resources.getColor(R.color.MAIN_WHITE))
            }
            else
            {
                binding.btnNext.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                binding.btnNext.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))

            }
        })

        vm.password.observe(this@ResetPasswordView, Observer { password ->

            val isValidate = ValidateUtil.isValidatePassword(password.toString())

            if (isValidate)
            {
                binding.btnNext.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnNext.setTextColor(resources.getColor(R.color.MAIN_WHITE))

                binding.tvPasswordHint.visibility = View.GONE
            }
            else
            {
                binding.btnNext.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                binding.btnNext.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))

                binding.tvPasswordHint.visibility = View.VISIBLE

            }

        })

        vm.navigator.observe(this, Observer {navigator ->
            when(navigator){
                NavigatorConst.TO_COMPLETE -> {
                    finish()
                }
                NavigatorConst.TO_BACK_SCREEN -> {
                    finish()
                }
            }
        })
    }

    private fun updateUIForInputPassword()
    {
        binding.layoutEmail.visibility = View.GONE
        binding.layoutPassword.visibility = View.VISIBLE

        binding.btnBack.visibility = View.GONE
        binding.btnClear.visibility = View.VISIBLE

        binding.tvSubTitle.visibility = View.GONE
        binding.btnNext.text = "변경하기"

        binding.tvPasswordHint.visibility = View.VISIBLE
    }

    private fun updateUIForComplete()
    {
        binding.btnNext.text = "확인"
        binding.layoutPassword.visibility = View.GONE
        binding.tvCompleteContent.visibility = View.VISIBLE

        binding.tvPasswordHint.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        SopoLog.d("""
            requestCode >>> $requestCode
            resultCode >>> $resultCode
            RESULT_OK >>> ${Activity.RESULT_OK}
        """.trimIndent())
        if(resultCode != Activity.RESULT_OK) return

        SopoLog.d("requestCode >>> $requestCode 성공")

        vm.resetType.postValue(1)

    }

}