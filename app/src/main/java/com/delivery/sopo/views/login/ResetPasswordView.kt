package com.delivery.sopo.views.login

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
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
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.login.ResetPasswordViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.menus.LockScreenView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResetPasswordView: BaseView<ResetPasswordViewBinding, ResetPasswordViewModel>()
{
    override val layoutRes: Int = R.layout.reset_password_view
    override val vm: ResetPasswordViewModel by viewModel()

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    val registerCallback = ActivityResultCallback<ActivityResult> { result ->
        if(result.resultCode != RESULT_OK)
        {
            return@ActivityResultCallback
        }

        result.data?.getStringExtra("JWT_TOKEN")?.also {

            vm.jwtTokenForReset = it
        }

        vm.resetType.postValue(1)
    }

    override fun receivedData(intent: Intent)
    {
    }

    override fun initUI()
    {
    }

    override fun setAfterSetUI()
    {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), registerCallback)
    }

    override fun setObserve()
    {
        vm.resetType.observe(this) {
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

        }

        vm.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this, focus)
            vm.validates[res.first] = res.second
        })

        vm.validateError.observe(this, Observer { target ->

            if(target.second)
            {
                return@Observer
            }

            val message = when(target.first)
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

            if(!result.result)
            {
                GeneralDialog(this, "오류", ResponseCode.UNKNOWN_ERROR.MSG,
                              ResponseCode.UNKNOWN_ERROR.CODE, Pair("네", null)).show(
                    supportFragmentManager, "DIALOG")
                return@Observer
            }

            when(vm.resetType.value)
            {
                0 ->
                {
                    if(result.data !is EmailAuthDTO)
                    {
                        GeneralDialog(this, "오류", "", result.code?.CODE, Pair("네", null)).show(
                            supportFragmentManager, "DIALOG")
                        return@Observer
                    }

                    val data: EmailAuthDTO = result.data

                    val intent = Intent(this@ResetPasswordView, LockScreenView::class.java).apply {
                        putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.RESET_ACCOUNT_PASSWORD)
                        putExtra("PIN_NUM", data.code)
                    }

                    activityResultLauncher?.launch(intent)

//                    launchActivityResult(intent, registerCallback)
                }
                1 ->
                {
                    vm.resetType.postValue(2)
                }
                2 ->
                {
                    updateUIForComplete()
                }
            }


        })

        vm.email.observe(this@ResetPasswordView, Observer { email ->

            val isValidate = ValidateUtil.isValidateEmail(email.toString())

            if(isValidate)
            {
                binding.btnNext.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnNext.setTextColor(resources.getColor(R.color.MAIN_WHITE))
                return@Observer
            }

            binding.btnNext.backgroundTintList =
                resources.getColorStateList(R.color.COLOR_GRAY_200, null)
            binding.btnNext.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))
        })

        vm.password.observe(this@ResetPasswordView, Observer { password ->

            val isValidate = ValidateUtil.isValidatePassword(password.toString())

            if(isValidate)
            {
                binding.btnNext.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnNext.setTextColor(ContextCompat.getColor(this, R.color.MAIN_WHITE))
                binding.tvPasswordHint.visibility = View.GONE
                return@Observer
            }

            binding.btnNext.backgroundTintList =
                resources.getColorStateList(R.color.COLOR_GRAY_200, null)
            binding.btnNext.setTextColor(ContextCompat.getColor(this, R.color.COLOR_GRAY_400))
            binding.tvPasswordHint.visibility = View.VISIBLE


        })

        vm.navigator.observe(this, Observer { navigator ->
            when(navigator)
            {
                NavigatorConst.TO_COMPLETE ->
                {
                    finish()
                }
                NavigatorConst.TO_BACK_SCREEN ->
                {
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
}