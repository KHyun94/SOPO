package com.delivery.sopo.presentation.views.menus

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.INVISIBLE
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.LockStatusConst
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.LockScreenViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.consts.IntentConst
import com.delivery.sopo.presentation.viewmodels.menus.LockScreenViewModel
import com.delivery.sopo.util.AnimationUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.VibrateUtil
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LockScreenView: BaseView<LockScreenViewBinding, LockScreenViewModel>()
{
    override val layoutRes: Int = R.layout.lock_screen_view
    override val vm: LockScreenViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainLockScreen }

    private var firstCheck = false
    private var firstPassword = ""

    override fun receivedData(intent: Intent)
    {
        val lockScreenStatus = intent.getSerializableExtra(IntentConst.Extra.LOCK_STATUS_TYPE) as LockScreenStatusEnum? ?: throw NullPointerException("'STATUS'가 존재하지 않습니다.")
        vm.setLockScreenStatus(lockScreenStatus)
    }

    override fun onAfterBinding()
    {
        super.onAfterBinding()

        noneOfNumberPadIsPressed()
    }

    override fun setObserve()
    {
        super.setObserve()

        vm.lockScreenStatus.observe(this) { status ->

            val (title, error, guide) = when(status)
            {
                LockScreenStatusEnum.SET_CONFIRM ->
                {
                    Triple("잠금번호 확인","잠금번호가 일치하지 않습니다.", "먼저, 기존 잠금번호를 확인합니다.")
                }
                LockScreenStatusEnum.SET_UPDATE ->
                {
                    Triple("잠금번호 변경","잠금번호가 일치하지 않습니다.", "변경할 잠금번호를 입력해 주세요.")
                }
                LockScreenStatusEnum.VERIFY ->
                {
                    Triple("잠금번호 입력","잠금번호가 일치하지 않습니다.", "어플 실행 잠금번호를 입력해 주세요.")
                }
            }

            vm.title.postValue(title)
            binding.tvErrorComment.text = error
            binding.tvGuideComment.text = guide
        }

        vm.lockNum.observe(this@LockScreenView) { lockNum ->
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
                                delay(200)
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
        }

        vm.verifyType.observe(this@LockScreenView, Observer {
            SopoLog.d("Verify Type Observe [data:$it]")
            when(it)
            {
                LockStatusConst.CONFIRM.CONFIRM_STATUS ->
                {

                }
                LockStatusConst.CONFIRM.FAILURE_STATUS ->
                {
                    binding.tvErrorComment.makeVisible()
                    binding.tvGuideComment.text = "다시 입력해 주세요."
                    VibrateUtil.startVibrate(context = this)
                    AnimationUtil.shakeHorizon(binding.tvErrorComment)


                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                         binding.tvGuideComment.text = "먼저, 기존 잠금번호를 확인합니다."
                        binding.tvErrorComment.makeGone()
                    }, 1000)
                }
                LockStatusConst.SET.VERIFY_STATUS ->
                {
                    binding.tvErrorComment.visibility = INVISIBLE
                    binding.tvGuideComment.text = "확인을 위해 한 번 더 입력해 주세요."
                }
                LockStatusConst.SET.FAILURE_STATUS ->
                {
                    binding.tvErrorComment.makeVisible()
                    binding.tvGuideComment.text = "처음부터 다시 시도해 주세요."
                    VibrateUtil.startVibrate(context = this)
                    AnimationUtil.shakeHorizon(binding.tvErrorComment)

                    firstPassword = ""
                    firstCheck = false

                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        binding.tvGuideComment.text = "변경할 잠금번호를 입력해 주세요."
                        binding.tvErrorComment.makeGone()
                    }, 1000)
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
                    binding.tvErrorComment.makeVisible()
                    binding.tvGuideComment.text = "다시 입력해 주세요."
                    VibrateUtil.startVibrate(context = this)
                    AnimationUtil.shakeHorizon(binding.tvErrorComment)

                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        binding.tvGuideComment.text = "어플 실행 잠금번호를 입력해 주세요."
                        binding.tvErrorComment.makeGone()
                    }, 1000)
                }
            }
        })

        vm.isButtonEnabled.observe(this){ isButtonEnabled ->
            isNumPadEnable(isButtonEnabled)
        }

        vm.error.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })

        vm.navigator.observe(this) {

            when(it)
            {
                "CANCEL" ->
                {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                NavigatorConst.Event.BACK ->
                {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }

        }
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