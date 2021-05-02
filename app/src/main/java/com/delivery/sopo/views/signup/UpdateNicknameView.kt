package com.delivery.sopo.views.signup

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.databinding.UpdateNicknameBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.signup.UpdateNicknameViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.widget.CustomEditText
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdateNicknameView: AppCompatActivity()
{
    private lateinit var binding: UpdateNicknameBinding
    private val vm: UpdateNicknameViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView<UpdateNicknameBinding>(this, R.layout.update_nickname)
        binding.vm = vm
        binding.lifecycleOwner = this

        binding.btnSndEmail.backgroundTintList =
            resources.getColorStateList(R.color.COLOR_GRAY_200, null)

        binding.btnSndEmail.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))

        setObserve()
    }

    private fun setObserve()
    {
        binding.etNickname.addTextChangedListener { nickname ->

            val isValidate = ValidateUtil.isValidateNickname(nickname.toString())

            if (isValidate)
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnSndEmail.setTextColor(resources.getColor(R.color.MAIN_WHITE))
            }
            else
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                binding.btnSndEmail.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))

            }
        }

        binding.vm!!.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this@UpdateNicknameView, focus)
            binding.vm!!.validates[res.first] = res.second
        })

        binding.vm!!.validateError.observe(this, Observer { target ->

            if (target.second)
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnSndEmail.setTextColor(resources.getColor(R.color.MAIN_WHITE))
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


        binding.vm!!.result.observe(this@UpdateNicknameView, Observer { result ->

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

                return@Observer
            }

            SopoLog.d("성공 닉네임 업데이트 여부 확인 ${result.result}, ${result.code}, ${result.message}")

            GeneralDialog(this@UpdateNicknameView, "성공", "정상적으로 닉네임을 등록했습니다.", null, Pair("네", object: OnAgreeClickListener
            {
                override fun invoke(agree: GeneralDialog)
                {
                    val intent = Intent(this@UpdateNicknameView, MainView::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
            })).show(supportFragmentManager, "DIALOG")
        })
    }
}