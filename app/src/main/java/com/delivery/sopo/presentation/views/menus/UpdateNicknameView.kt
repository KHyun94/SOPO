package com.delivery.sopo.presentation.views.menus

import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.UpdateNicknameViewBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.OptionalTypeEnum
import com.delivery.sopo.extensions.launchActivityWithAllClear
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.presentation.viewmodels.menus.UpdateNicknameViewModel
import com.delivery.sopo.presentation.views.dialog.GeneralDialog
import com.delivery.sopo.presentation.views.dialog.OptionalDialog
import com.delivery.sopo.presentation.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdateNicknameView: BaseView<UpdateNicknameViewBinding, UpdateNicknameViewModel>()
{
    override val layoutRes: Int = R.layout.update_nickname_view
    override val vm: UpdateNicknameViewModel by viewModel()
    override val mainLayout: View by lazy{ binding.constraintMainUpdateNickname}

    override fun onAfterBinding()
    {
        super.onAfterBinding()

        binding.btnSndEmail.backgroundTintList = resources.getColorStateList(R.color.COLOR_GRAY_200, null)
        binding.btnSndEmail.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))

        binding.etNickname.addTextChangedListener { nickname ->

            val isValidate = ValidateUtil.isValidateNickname(nickname.toString())

            if(isValidate)
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                binding.btnSndEmail.setTextColor(ContextCompat.getColor(this, R.color.MAIN_WHITE))
            }
            else
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                binding.btnSndEmail.setTextColor(ContextCompat.getColor(this, R.color.COLOR_GRAY_400))
            }
        }
    }

    override fun setObserve()
    {
        super.setObserve()

        vm.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(this@UpdateNicknameView, focus)
            vm.validates[res.first] = res.second
        })

        vm.validateError.observe(this, Observer { target ->

            if(target.second)
            {
                binding.btnSndEmail.backgroundTintList =
                    resources.getColorStateList(R.color.COLOR_MAIN_700, null)
                val colorRes = ContextCompat.getColor(this, R.color.MAIN_WHITE)
                binding.btnSndEmail.setTextColor(colorRes)
                return@Observer
            }

            val message = when(target.first)
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

        vm.navigator.observe(this@UpdateNicknameView, Observer { navigator ->
            when(navigator)
            {
                NavigatorConst.TO_MAIN ->
                {
                    val builder = SpannableStringBuilder("변경된 닉네임은\n${vm.nickname.value?.toString()}입니다.")
                    builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.COLOR_MAIN_700)), 8, builder.length - 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


                    val optionalDialog = OptionalDialog(optionalType = OptionalTypeEnum.ONE_WAY,
                                                        title = builder,
                                                        leftHandler = Pair("확인"){
                                                            Intent(this@UpdateNicknameView, MainView::class.java).launchActivityWithAllClear(this@UpdateNicknameView)
                                                            finish()
                                                        })

                    optionalDialog.show(supportFragmentManager, "")
                }
                NavigatorConst.TO_BACK_SCREEN ->
                {
                    finish()
                }
            }
        })

        vm.result.observe(this@UpdateNicknameView, Observer { result ->

            if(!result.result)
            {
                SopoLog.d("실패 닉네임 업데이트 여부 확인 ${result.result}, ${result.code}, ${result.message}")

                when(result.displayType)
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