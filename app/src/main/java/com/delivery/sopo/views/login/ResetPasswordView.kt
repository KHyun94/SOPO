package com.delivery.sopo.views.login

import android.app.Activity
import android.content.Intent
import android.os.CountDownTimer
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
import com.delivery.sopo.extensions.convertTextColor
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
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
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResetPasswordView: BaseView<ResetPasswordViewBinding, ResetPasswordViewModel>()
{
    override val layoutRes: Int = R.layout.reset_password_view
    override val vm: ResetPasswordViewModel by viewModel()
    override val mainLayout: View by lazy { binding.layoutMainReset }

    var timer: CountDownTimer? = null

//    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

//    private val registerCallback = ActivityResultCallback<ActivityResult> { result ->
//
//        SopoLog.d("activityResult => ${result.resultCode}")
//
//        if(result.resultCode == Activity.RESULT_CANCELED)
//        {
//            vm.setNavigator(ResetPasswordConst.INPUT_EMAIL_FOR_SEND)
//            binding.etEmail.setText("")
//            binding.etEmail.requestFocus()
//
//            return@ActivityResultCallback
//        }
//
//        vm.authToken = result.data?.getStringExtra("AUTH_TOKEN") ?: ""
//        vm.setNavigator(ResetPasswordConst.INPUT_PASSWORD_FOR_RESET)
//
//        return@ActivityResultCallback
//    }

    override fun onBeforeBinding()
    {
        super.onBeforeBinding()
//        activityResultLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), registerCallback)
    }

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

//        vm.email.observe(this@ResetPasswordView, Observer { email ->
//
//            val isValidate = ValidateUtil.isValidateEmail(email.toString())
//
//            if(isValidate)
//            {
//                binding.btnNext.backgroundTintList =
//                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
//                binding.btnNext.setTextColor(ContextCompat.getColor(this, R.color.MAIN_WHITE))
//                return@Observer
//            }
//
//            binding.btnNext.backgroundTintList =
//                resources.getColorStateList(R.color.COLOR_GRAY_200, null)
//            binding.btnNext.setTextColor(ContextCompat.getColor(this, R.color.COLOR_GRAY_400))
//        })

//        vm.password.observe(this@ResetPasswordView, Observer { password ->
//
//            val isValidate = ValidateUtil.isValidatePassword(password.toString())
//
//            if(isValidate)
//            {
//                binding.btnNext.backgroundTintList = resources.getColorStateList(R.color.COLOR_MAIN_700, null)
//                binding.btnNext.setTextColor(ContextCompat.getColor(this, R.color.MAIN_WHITE))
//                binding.tvPasswordHint.makeGone()
//                return@Observer
//            }
//
//            binding.btnNext.backgroundTintList =
//                resources.getColorStateList(R.color.COLOR_GRAY_200, null)
//            binding.btnNext.setTextColor(ContextCompat.getColor(this, R.color.COLOR_GRAY_400))
//            binding.tvPasswordHint.makeVisible()
//
//
//        })

        binding.etAuthCode.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus)
            {
                binding.relativeMainAuthCode.background = ContextCompat.getDrawable(this@ResetPasswordView, R.drawable.border_all_round_10dp_blue_scale)
                binding.etAuthCode.convertTextColor(R.color.COLOR_GRAY_800)
            }
            else
            {
                binding.relativeMainAuthCode.background = ContextCompat.getDrawable(this@ResetPasswordView, R.drawable.border_all_round_10dp)
                binding.etAuthCode.convertTextColor(R.color.COLOR_GRAY_400)
            }
        }

        vm.navigator.observe(this) { navigator ->

            SopoLog.i("navigator Observe [data:${navigator}]")

            when(navigator)
            {
                ResetPasswordConst.INPUT_EMAIL_FOR_SEND ->
                {
//                    updateUIForInputEmail()
                }
                ResetPasswordConst.INPUT_AUTH_CODE ->
                {
                    if(!isCountTimer) timer = countLimitTime()
                    timer?.start()

//                    val intent = Intent(this@ResetPasswordView, LockScreenView::class.java).apply {
//                        putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.RESET_ACCOUNT_PASSWORD)
//                        putExtra("JWT_TOKEN", vm.jwtToken)
//                        putExtra("EMAIL", vm.email.value)
//                    }
//
//                    activityResultLauncher?.launch(intent)
                }
                ResetPasswordConst.INPUT_PASSWORD_FOR_RESET ->
                {
//                    updateUIForInputPassword()
                }
                ResetPasswordConst.COMPLETED_RESET_PASSWORD ->
                {
                }
                NavigatorConst.TO_COMPLETE, NavigatorConst.TO_BACK_SCREEN ->
                {
                    finish()
                }


            }
        }
    }

    var isCountTimer = false

    fun countLimitTime(): CountDownTimer
    {
       return object: CountDownTimer(180000, 1000){
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
               else if(currentMinutes == 0L && currentSeconds > 0)
               {
                    "${currentSeconds}초 내 입력"
               } else
               {
                   "타임오버"
               }

               binding.tvCountOfAuth.text = time
           }
           override fun onFinish()
           {
               isCountTimer = false
           }

       }
    }
}