package com.delivery.sopo.views.menus

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.LockStatusConst
import com.delivery.sopo.databinding.LockScreenViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.LockScreenViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LockScreenView: BaseView<LockScreenViewBinding, LockScreenViewModel>()
{
    override val layoutRes: Int = R.layout.lock_screen_view
    override val vm: LockScreenViewModel by viewModel()

    // 정체를 몰라 건드리지를 못하겠다.
    private var firstCheck = false
    private var firstPassword = ""

    override fun receivedData(intent: Intent)
    {
        SopoLog.i("receivedData(...) 호출")

        // TODO 에러 처리
        val lockScreenStatus = intent.getSerializableExtra(IntentConst.LOCK_SCREEN) as LockScreenStatusEnum? ?: throw NullPointerException("'STATUS'가 존재하지 않습니다.")
        val pinCode = intent.getStringExtra("PIN_NUM") ?: ""

        SopoLog.d("[status:$lockScreenStatus][pinCode:$pinCode]")

        vm.pinCode = pinCode
        vm.setLockScreenStatus(lockScreenStatus)
    }

    override fun initUI()
    {
        SopoLog.i("initUI(...) 호출")

        noneOfNumberPadIsPressed()

        val (title, error, guide) = when(vm.lockScreenStatus.value)
        {
            LockScreenStatusEnum.SET ->
            {
                Triple("잠금번호 입력","잠금번호가 일치하지 않습니다.", "어플 실행 잠금번호를 입력해 주세요.")
            }
            LockScreenStatusEnum.VERIFY ->
            {
                Triple("잠금번호 입력","잠금번호가 일치하지 않습니다.", "어플 실행 잠금번호를 입력해 주세요.")
            }
            LockScreenStatusEnum.RESET_ACCOUNT_PASSWORD ->
            {
                Triple("인증 코드 입력","인증코드가 일치하지 않습니다.", "이메일로 받은 4자리 코드를 입력해주세요.")
            }
            else -> throw IllegalArgumentException("'STATUS'가 존재하지 않습니다.")
        }

        binding.tvTitleLock.text = title
        binding.tvErrorComment.text = error
        binding.tvGuideComment.text = guide
    }

    override fun setAfterSetUI()
    {
    }

    override fun setObserve()
    {
        vm.isActivateResendMail.observe(this){ isActivityResendMail ->
            if(isActivityResendMail) binding.tvResendMail.visibility = View.VISIBLE
            else binding.tvResendMail.visibility = View.GONE
        }

        vm.lockNum.observe(this@LockScreenView, Observer { lockNum ->
            val scope = CoroutineScope(Dispatchers.Main)

            when(lockNum.length)
            {
                0 ->
                {
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            if(firstCheck)
                            {
                                isNumPadEnable(false)
                                delay(500)
                                isNumPadEnable(true)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            noneOfNumberPadIsPressed()
                        }
                    }
                }
                1 ->
                {
                    scope.launch {
                        firstPasswordIsPressed()
                    }
                }
                2 ->
                {
                    scope.launch {
                        secondPasswordIsPressed()
                    }
                }
                3 ->
                {
                    scope.launch {
                        thirdPasswordIsPressed()
                    }
                }
                4 ->
                {
                    scope.launch {
                        if(!firstCheck && firstPassword == "")
                        {
                            firstCheck = true
                            firstPassword = lockNum
                        }
                        fourthPasswordIsPressed()
                    }
                }
            }
        })

        vm.verifyType.observe(this@LockScreenView, Observer {
            SopoLog.d("Verify Type Observe [data:$it]")
            when(it)
            {
                LockStatusConst.SET.VERIFY_STATUS ->
                {
                    binding.tvErrorComment.visibility = INVISIBLE
                    binding.tvGuideComment.text = "확인을 위해 한 번 더 입력해 주세요."
                }
                LockStatusConst.SET.FAILURE_STATUS ->
                {
                    binding.tvErrorComment.visibility = VISIBLE
                    binding.tvGuideComment.text = "처음부터 다시 시도해 주세요."
                    firstPassword = ""
                    firstCheck = false
                }
                LockStatusConst.SET.CONFIRM_STATUS ->
                {
                    finish()
                }
                LockStatusConst.VERIFY.CONFIRM_STATUS ->
                {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                LockStatusConst.VERIFY.FAILURE_STATUS ->
                {
                    binding.tvErrorComment.visibility = VISIBLE
                    binding.tvGuideComment.text = "다시 입력해 주세요."
                }
                LockStatusConst.AUTH.CONFIRM_STATUS ->
                {
                    val intent = Intent().apply { putExtra("JWT_TOKEN", vm.jwtToken) }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                LockStatusConst.AUTH.FAILURE_STATUS ->
                {
                    binding.tvErrorComment.visibility = VISIBLE
                    binding.tvGuideComment.text = "다시 입력해 주세요."
                }
            }
        })

        vm.isButtonEnabled.observe(this){ isButtonEnabled ->
            isNumPadEnable(isButtonEnabled)
        }

        vm.error.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })
    }

    private fun noneOfNumberPadIsPressed()
    {
        binding.etFirstPassword.background =
            ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        binding.etSecondPassword.background =
            ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        binding.etThirdPassword.background =
            ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        binding.etFourthPassword.background =
            ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun firstPasswordIsPressed()
    {
        binding.etFirstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        binding.etSecondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        binding.etThirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        binding.etFourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun secondPasswordIsPressed()
    {
        binding.etFirstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        binding.etSecondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        binding.etThirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        binding.etFourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun thirdPasswordIsPressed()
    {
        binding.etFirstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        binding.etSecondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        binding.etThirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        binding.etFourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun fourthPasswordIsPressed()
    {
        binding.etFirstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        binding.etSecondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        binding.etThirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        binding.etFourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
    }

    private fun isNumPadEnable(enable: Boolean)
    {
        binding.btn0.isEnabled = enable
        binding.btn1.isEnabled = enable
        binding.btn2.isEnabled = enable
        binding.btn3.isEnabled = enable
        binding.btn4.isEnabled = enable
        binding.btn5.isEnabled = enable
        binding.btn6.isEnabled = enable
        binding.btn7.isEnabled = enable
        binding.btn8.isEnabled = enable
        binding.btn9.isEnabled = enable
    }

    override fun onBackPressed()
    {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }
}