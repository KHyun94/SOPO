package com.delivery.sopo.views.menus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.RegisterNicknameViewBinding
import com.delivery.sopo.databinding.UpdateNicknameViewBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.menus.UpdateNicknameViewModel
import com.delivery.sopo.viewmodels.signup.RegisterNicknameViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdateNicknameView: AppCompatActivity()
{
    private lateinit var binding: UpdateNicknameViewBinding
    private val vm: UpdateNicknameViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<UpdateNicknameViewBinding>(this, R.layout.update_nickname_view)
        binding.vm = vm
        binding.lifecycleOwner = this

        binding.btnSndEmail.backgroundTintList = resources.getColorStateList(R.color.COLOR_GRAY_200, null)

        binding.btnSndEmail.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))

        setObserve()
    }

    private fun setObserve()
    {
        binding.etNickname.addTextChangedListener { nickname ->

            val isValidate = ValidateUtil.isValidateNickname(nickname.toString())

            if (isValidate)
            {
                binding.btnSndEmail.backgroundTintList = resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnSndEmail.setTextColor(resources.getColor(R.color.MAIN_WHITE))
            }
            else
            {
                binding.btnSndEmail.backgroundTintList = resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                binding.btnSndEmail.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))

            }
        }

        vm.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this@UpdateNicknameView, focus)
            vm.validates[res.first] = res.second
        })

        vm.validateError.observe(this, Observer { target ->

            if (target.second)
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                val colorRes = ContextCompat.getColor(this, R.color.MAIN_WHITE)
                binding.btnSndEmail.setTextColor(colorRes)
                return@Observer
            }

            val message = when (target.first)
            {
                InfoEnum.NICKNAME ->
                {
                    binding.btnSndEmail.backgroundTintList =
                        resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                    binding.btnSndEmail.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))

                    binding.etNickname.requestFocus()
                    "닉네임을 확인해주세요."
                }
                else -> ""
            }

            Toast.makeText(this@UpdateNicknameView, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        })

        vm.navigator.observe(this@UpdateNicknameView, Observer {navigator ->
            when(navigator){
                NavigatorConst.TO_COMPLETE -> {
                    GeneralDialog(this@UpdateNicknameView, "성공", "정상적으로 닉네임을 등록했습니다.", null, Pair("네", object: OnAgreeClickListener
                    {
                        override fun invoke(agree: GeneralDialog)
                        {
                            finish()
                        }
                    })).show(supportFragmentManager, "DIALOG")
                }
                NavigatorConst.TO_BACK_SCREEN -> {
                    finish()
                }
            }
        })

        vm.result.observe(this@UpdateNicknameView, Observer { result ->

            if (!result.result)
            {
                SopoLog.d("실패 닉네임 업데이트 여부 확인 ${result.result}, ${result.code}, ${result.message}")

                when (result.displayType)
                {
                    DisplayEnum.TOAST_MESSAGE ->
                    {
                        Toast.makeText(this@UpdateNicknameView, "정보 입력을 완료해주세요.", Toast.LENGTH_LONG)
                            .apply {
                                setGravity(Gravity.TOP, 0, 180)
                            }
                            .show()
                    }
                    DisplayEnum.DIALOG ->
                    {
                        GeneralDialog(this@UpdateNicknameView, "오류", "닉네임 등록이 실패했습니다.\n다시 시도해주세요.", null, Pair("네", null)).show(supportFragmentManager, "DIALOG")
                    }
                }
            }
        })
    }
}