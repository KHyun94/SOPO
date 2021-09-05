package com.delivery.sopo.views.menus

import android.app.Activity
import android.content.Intent
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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
import kotlinx.android.synthetic.main.lock_screen_view.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LockScreenView: BaseView<LockScreenViewBinding, LockScreenViewModel>()
{
    override val layoutRes: Int = R.layout.lock_screen_view
    override val vm: LockScreenViewModel by viewModel()

    private var firstCheck = false
    private var firstPassword = ""

    override fun receivedData(intent: Intent)
    {
        val lockScreenStatus = intent.getSerializableExtra(IntentConst.LOCK_SCREEN) as LockScreenStatusEnum? ?: throw IllegalArgumentException("접근이 잘못되었습니다.")
        val pinCode = intent.getStringExtra("PIN_NUM")
        SopoLog.d("receivedData(...) 호출 [status:$lockScreenStatus][pinCode:$pinCode]")

        vm.pinCode.value = pinCode
        vm.setLockScreenStatus(lockScreenStatus)
    }

    override fun initUI()
    {
    }

    override fun setAfterSetUI()
    {
    }

    override fun setObserve()
    {
        vm.inputLockNum.observe(this@LockScreenView, Observer {
            val scope = CoroutineScope(Dispatchers.Main)

            SopoLog.d("Input Lock Number [data:$it][길이:${it.length}")

            when(it.length)
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
                            firstPassword = it
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
                    tv_errorComment.visibility = INVISIBLE
                    tv_guide_comment.text = "확인을 위해 한 번 더 입력해 주세요."
                }
                LockStatusConst.SET.FAILURE_STATUS ->
                {
                    tv_errorComment.visibility = VISIBLE
                    tv_guide_comment.text = "처음부터 다시 시도해 주세요."
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
                    tv_errorComment.visibility = VISIBLE
                    tv_guide_comment.text = "다시 입력해 주세요."
                }
            }
        })
    }

    private fun noneOfNumberPadIsPressed()
    {
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun firstPasswordIsPressed()
    {
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun secondPasswordIsPressed()
    {
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun thirdPasswordIsPressed()
    {
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun fourthPasswordIsPressed()
    {
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
    }

    private fun isNumPadEnable(enable: Boolean)
    {
        btn_0.isEnabled = enable
        btn_1.isEnabled = enable
        btn_2.isEnabled = enable
        btn_3.isEnabled = enable
        btn_4.isEnabled = enable
        btn_5.isEnabled = enable
        btn_6.isEnabled = enable
        btn_7.isEnabled = enable
        btn_8.isEnabled = enable
        btn_9.isEnabled = enable
    }

    override fun onBackPressed()
    {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }
}