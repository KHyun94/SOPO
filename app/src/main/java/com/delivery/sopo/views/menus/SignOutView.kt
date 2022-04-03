package com.delivery.sopo.views.menus

import android.content.res.ColorStateList
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.SignOutViewBinding
import com.delivery.sopo.enums.OptionalTypeEnum
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.viewmodels.menus.SignOutViewModel
import com.delivery.sopo.views.dialog.OnOptionalClickListener
import com.delivery.sopo.views.dialog.OptionalDialog
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

//                    val subtitle = Html.fromHtml("고객의 정보가 삭제되어\n<font color=\"#5C92F6\">영구히 복구 불가능</font>합니다.")

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
                NavigatorConst.TO_BACK_SCREEN ->
                {
                    finish()
                }
            }
        }

        /*vm.result.observe(this, Observer { res ->

            if(!res.result)
            {

                return@Observer
            }

            when(res.displayType)
            {
                DisplayEnum.TOAST_MESSAGE ->
                {
                    Intent(this@SignOutView, LoginSelectView::class.java).let {
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(it)
                        finish()
                    }

                    Toast.makeText(this, "지금까지 사용해주셔서 감사합니다.", Toast.LENGTH_LONG).apply {
                        setGravity(Gravity.TOP, 0, 180)
                    }.show()
                }
                DisplayEnum.DIALOG ->
                {
                    OptionalDialog(optionalType = OptionalTypeEnum.RIGHT, titleIcon = 0, title = "탈퇴 시 유의사항", subTitle = "고객의 정보가 삭제되며 복구가 불가능합니다.", content = """
                    * 계정 개인정보(이메일, 비밀번호)
                    * 등록하신 모든 택배 추적 정보
                                """.trimIndent(), leftHandler = Pair("삭제할게요", object:
                            OptionalClickListener
                    {
                        override fun invoke(dialog: OptionalDialog)
                        {
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.vm!!.requestSignOut(res.data as String)
                            }
                            dialog.dismiss()
                        }
                    }), rightHandler = Pair("다시 생각할게요", object: OptionalClickListener
                    {
                        override fun invoke(dialog: OptionalDialog)
                        {
                            dialog.dismiss()
                        }
                    })).show(supportFragmentManager, "")
                }
            }


        })*/

        vm.message.observe(this, Observer { message ->
            if(message != "")
            {
                binding.tvBtn.run {
                    setBackgroundResource(R.drawable.border_15dp_blue_rounder)
                    backgroundTintList = null
                    setTextColor(resources.getColor(R.color.COLOR_MAIN_700))
                }
                return@Observer
            }

            binding.tvBtn.run {
                setBackgroundResource(R.drawable.border_15dp_blue_rounder)
                backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this@SignOutView, R.color.COLOR_GRAY_200))
                setTextColor(resources.getColor(R.color.COLOR_GRAY_400))
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