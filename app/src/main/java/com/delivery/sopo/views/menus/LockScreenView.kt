package com.delivery.sopo.views.menus

import android.os.Bundle
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.databinding.LockScreenViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.viewmodels.menus.LockScreenViewModel
import kotlinx.android.synthetic.main.lock_screen_view.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LockScreenView : AppCompatActivity()
{
    private val lockScreenVM: LockScreenViewModel by viewModel()
    private lateinit var binding: LockScreenViewBinding
    private var firstCheck = false
    private var firstPassword = ""

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.lock_screen_view)
        bindView()
        loadData()
        setObserver()
    }

    private fun bindView() {
        binding.vm = lockScreenVM
        binding.lifecycleOwner = this
        binding.executePendingBindings() // 즉 바인딩
    }

    fun setObserver() {
        lockScreenVM.lockPassword.observe(this@LockScreenView, Observer {
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
        lockScreenVM.verifyResult.observe(this, Observer{
            if(it){
                finish()
            }
            else{
                tv_errorComment.visibility = VISIBLE
                tv_guideComment.text = "다시 입력해 주세요."
            }
        })

        lockScreenVM.lockScreenStatusEnum.observe(this, Observer { status ->

            when(status)
            {
                LockScreenStatusEnum.RESET ->
                {

                }
                else ->
                {

                }
            }
        })

        lockScreenVM.verifyCnt.observe(this@LockScreenView, Observer {

            if(binding.vm!!.lockScreenStatusEnum.value == LockScreenStatusEnum.RESET)
            {

                return@Observer
            }

            when(it){
                1 ->{
                    tv_errorComment.visibility = INVISIBLE
                    tv_guideComment.text = "확인을 위해 한 번 더 입력해 주세요."
                }
                2 -> {
                    tv_errorComment.visibility = VISIBLE
                    tv_guideComment.text = "처음부터 다시 시도해 주세요."
                    firstPassword = ""
                    firstCheck = false
                }
                3-> {
                    finish()
                }
            }
        })
    }

    private fun loadData() {
        lockScreenVM.setLockScreenStatus(intent.getSerializableExtra(IntentConst.LOCK_SCREEN) as LockScreenStatusEnum)
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