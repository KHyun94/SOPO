package com.delivery.sopo.views.menus

import android.app.Activity
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.databinding.LockScreenViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.LockScreenViewModel
import kotlinx.android.synthetic.main.lock_screen_view.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LockScreenView : AppCompatActivity()
{
    private val vm: LockScreenViewModel by viewModel()
    private lateinit var binding: LockScreenViewBinding
    private var firstCheck = false
    private var firstPassword = ""

    var pinNumByEmail:String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = bindView(this, R.layout.lock_screen_view, vm)
        receivedData()

        pinNumByEmail = intent.getStringExtra("PIN_NUM")
        vm.pinCode.value = pinNumByEmail

        setObserver()
    }

    fun <T : ViewDataBinding> bindView(activity: FragmentActivity, @LayoutRes layoutId : Int, vm: ViewModel) : T
    {
        return DataBindingUtil.setContentView<T>(activity,layoutId).apply{
            this.lifecycleOwner = lifecycleOwner
            this.setVariable(BR.vm, vm)
            executePendingBindings()
        }
    }

    private fun receivedData() {
        val data = intent.getSerializableExtra(IntentConst.LOCK_SCREEN) as LockScreenStatusEnum? ?: throw IllegalArgumentException("접근이 잘못되었습니다.")
        SopoLog.d("receivedData(...) 호출 [data:$data]")
        vm.setLockScreenStatus(data)
    }

    fun setObserver() {

        vm.lockPassword.observe(this@LockScreenView, Observer {
            val scope = CoroutineScope(Dispatchers.Main)

            when (it.length)
            {
                0->{
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            if (firstCheck) {
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
                1 ->{
                    scope.launch {
                        firstPasswordIsPressed()
                    }
                }
                2 -> {
                    scope.launch {
                        secondPasswordIsPressed()
                    }
                }
                3 -> {
                    scope.launch {
                        thirdPasswordIsPressed()
                    }
                }
                4 ->{
                    scope.launch {
                        if(!firstCheck && firstPassword == ""){
                            firstCheck = true
                            firstPassword = it
                        }
                        fourthPasswordIsPressed()
                    }
                }
            }
        })

        // 인증 여부
        vm.verifyResult.observe(this, Observer{
            if(it){
                setResult(Activity.RESULT_OK)
                finish()
            }
            else{
                tv_errorComment.visibility = VISIBLE
                tv_guide_comment.text = "다시 입력해 주세요."
            }
        })



        vm.verifyCnt.observe(this@LockScreenView, Observer {

            if(vm.lockScreenStatusEnum.value == LockScreenStatusEnum.RESET_ACCOUNT_PASSWORD)
            {

                return@Observer
            }

            when(it){
                1 ->{
                    tv_errorComment.visibility = INVISIBLE
                    tv_guide_comment.text = "확인을 위해 한 번 더 입력해 주세요."
                }
                2 -> {
                    tv_errorComment.visibility = VISIBLE
                    tv_guide_comment.text = "처음부터 다시 시도해 주세요."
                    firstPassword = ""
                    firstCheck = false
                }
                3-> {
                    finish()
                }
            }
        })
    }

    private fun noneOfNumberPadIsPressed(){
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun firstPasswordIsPressed(){
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun secondPasswordIsPressed(){
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }

    private fun thirdPasswordIsPressed(){
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_off)
    }
    private fun fourthPasswordIsPressed(){
        et_firstPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_secondPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_thirdPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
        et_fourthPassword.background = ContextCompat.getDrawable(this, R.drawable.ic_lock_edittext_on)
    }

    private fun isNumPadEnable(enable: Boolean){
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
}