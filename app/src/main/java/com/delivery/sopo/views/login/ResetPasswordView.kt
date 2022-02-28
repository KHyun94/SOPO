package com.delivery.sopo.views.login

import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.ResetPasswordConst
import com.delivery.sopo.databinding.ResetPasswordViewBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.models.base.OnActivityResultCallbackListener
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.login.ResetPasswordViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.menus.LockScreenView
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResetPasswordView: BaseView<ResetPasswordViewBinding, ResetPasswordViewModel>()
{
    override val layoutRes: Int = R.layout.reset_password_view
    override val vm: ResetPasswordViewModel by viewModel()
    override val mainLayout: View by lazy { binding.layoutMainReset }

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    private val registerCallback = ActivityResultCallback<ActivityResult> { result ->

        SopoLog.d("activityResult => ${result.resultCode}")

        if(result.resultCode == Activity.RESULT_CANCELED)
        {
            vm.setNavigator(ResetPasswordConst.INPUT_EMAIL_FOR_SEND)
            binding.etEmail.setText("")
            binding.etEmail.requestFocus()

            return@ActivityResultCallback
        }

        vm.authToken = result.data?.getStringExtra("AUTH_TOKEN") ?: ""
        vm.setNavigator(ResetPasswordConst.INPUT_PASSWORD_FOR_RESET)

        return@ActivityResultCallback
    }

    override fun onBeforeBinding()
    {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), registerCallback)
    }

    override fun setObserve()
    {
        super.setObserve()

        vm.focus.observe(this) { focus ->
            val res = TextInputUtil.changeFocus(this, focus)
            vm.validity[res.first] = res.second
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

        vm.email.observe(this@ResetPasswordView, Observer { email ->

            val isValidate = ValidateUtil.isValidateEmail(email.toString())

            if(isValidate)
            {
                binding.btnNext.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnNext.setTextColor(ContextCompat.getColor(this, R.color.MAIN_WHITE))
                return@Observer
            }

            binding.btnNext.backgroundTintList =
                resources.getColorStateList(R.color.COLOR_GRAY_200, null)
            binding.btnNext.setTextColor(ContextCompat.getColor(this, R.color.COLOR_GRAY_400))
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

        vm.navigator.observe(this) { navigator ->

            SopoLog.i("navigator Observe [data:${navigator}]")

            when(navigator)
            {

                ResetPasswordConst.INPUT_EMAIL_FOR_SEND ->
                {
                    updateUIForInputEmail()
                }
                ResetPasswordConst.SEND_AUTH_EMAIL ->
                {
                    val intent = Intent(this@ResetPasswordView, LockScreenView::class.java).apply {
                        putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.RESET_ACCOUNT_PASSWORD)
                        putExtra("JWT_TOKEN", vm.jwtToken)
                        putExtra("EMAIL", vm.email.value)
                    }

                    activityResultLauncher?.launch(intent)
                }
                ResetPasswordConst.INPUT_PASSWORD_FOR_RESET ->
                {
                    updateUIForInputPassword()
                }
                ResetPasswordConst.COMPLETED_RESET_PASSWORD ->
                {
                    updateUIForComplete()
                }
                NavigatorConst.TO_COMPLETE, NavigatorConst.TO_BACK_SCREEN ->
                {
                    finish()
                }


            }
        }
    }

    private fun updateUIForInputEmail()
    {
        binding.btnNext.text = "재설정코드발송"

//        binding.layoutEmail.visibility = View.VISIBLE
//        binding.layoutPassword.visibility = View.GONE
//        binding.tvSubTitle.visibility = View.GONE
//        binding.tvPasswordHint.visibility = View.GONE
    }

    private fun updateUIForInputPassword()
    {
        binding.btnNext.text = "변경하기"

//        binding.layoutEmail.visibility = View.GONE
//        binding.layoutPassword.visibility = View.VISIBLE

//        binding.tvSubTitle.visibility = View.GONE
//        binding.tvPasswordHint.visibility = View.VISIBLE
    }

    private fun updateUIForComplete()
    {
        binding.btnNext.text = "확인"

//        binding.linearCompleteReset.visibility = View.VISIBLE
//        binding.layoutPassword.visibility = View.GONE
//        binding.tvPasswordHint.visibility = View.GONE
    }
}