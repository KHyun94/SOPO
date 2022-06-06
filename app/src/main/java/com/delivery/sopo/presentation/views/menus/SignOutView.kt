package com.delivery.sopo.presentation.views.menus

import android.content.res.ColorStateList
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.SignOutViewBinding
import com.delivery.sopo.enums.OptionalTypeEnum
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.viewmodels.menus.SignOutViewModel
import com.delivery.sopo.presentation.views.dialog.OnOptionalClickListener
import com.delivery.sopo.presentation.views.dialog.OptionalDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignOutView: BaseView<SignOutViewBinding, SignOutViewModel>()
{
    override val layoutRes: Int = R.layout.sign_out_view
    override val vm: SignOutViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainSignOut }

    override fun setObserve()
    {
        super.setObserve()

        vm.navigator.observe(this) { nav ->

            when(nav)
            {
                NavigatorConst.CONFIRM_SIGN_OUT ->
                {
                    val builder = SpannableStringBuilder("고객의 정보가 삭제되어\n영구히 복구 불가능합니다.")
                    builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.COLOR_MAIN_700)), 12, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    OptionalDialog(optionalType = OptionalTypeEnum.TWO_WAY_RIGHT,  title = "잠깐만요!", subtitle = builder, content = """
                    * 계정 개인정보(이메일, 비밀번호)
                    * 등록하신 모든 택배 추적 정보
                    """.trimIndent(), leftHandler = Pair("삭제할게요", object: OnOptionalClickListener {
                        override fun invoke(dialog: DialogFragment)
                        {
                            vm.requestSignOut(vm.message.value.toString())
                            dialog.dismiss()
                        }
                    }), rightHandler = Pair("다시 생각할게요", object: OnOptionalClickListener
                    {
                        override fun invoke(dialog: DialogFragment)
                        {
                            dialog.dismiss()
                        }
                    })).show(supportFragmentManager, "")
                }
                NavigatorConst.EXIT ->
                {
                    Toast.makeText(this, "지금까지 사용해주셔서 감사합니다.", Toast.LENGTH_LONG).apply {
                        setGravity(Gravity.TOP, 0, 180)
                    }.show()

                    exit()
                }
                NavigatorConst.Event.BACK ->
                {
                    finish()
                }
            }
        }

        vm.message.observe(this, Observer { message ->
            if(message != "")
            {
                binding.tvBtn.run {
                    setBackgroundResource(R.drawable.border_15dp_blue_rounder)
                    backgroundTintList = null
                    setTextColor(resources.getColor(R.color.COLOR_MAIN_700))
                    isEnabled = true
                }
                return@Observer
            }

            binding.tvBtn.run {
                setBackgroundResource(R.drawable.border_15dp_blue_rounder)
                backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this@SignOutView, R.color.COLOR_GRAY_200))
                setTextColor(resources.getColor(R.color.COLOR_GRAY_400))
                isEnabled = false
            }

            vm.otherReason.observe(this, Observer {
                if(it.isNotEmpty())
                {
                    binding.vm!!.message.postValue(it)
                }
            })

        })
    }


}