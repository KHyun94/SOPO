package com.delivery.sopo.views.menus

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.SignOutViewBinding
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.OptionalTypeEnum
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.viewmodels.menus.SignOutViewModel
import com.delivery.sopo.views.dialog.OptionalClickListener
import com.delivery.sopo.views.dialog.OptionalDialog
import com.delivery.sopo.views.login.LoginSelectView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignOutView: AppCompatActivity()
{
    lateinit var binding: SignOutViewBinding
    private val vm: SignOutViewModel by viewModel()

    private val userLocalRepo: UserLocalRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        bindView()
        setObserve()
    }

    fun bindView()
    {
        binding = DataBindingUtil.setContentView(this, R.layout.sign_out_view)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    fun setObserve()
    {
        binding.vm!!.result.observe(this, Observer { res ->

            if(!res.result)
            {

                return@Observer
            }

            when(res.displayType)
            {
                DisplayEnum.TOAST_MESSAGE ->
                {
                    userLocalRepo.removeUserRepo()

                    CoroutineScope(Dispatchers.Default).launch { AlertUtil.appDataBase.clearAllTables() }

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
                    OptionalDialog(
                        optionalType = OptionalTypeEnum.RIGHT,
                        titleIcon = 0,
                        title = "탈퇴 시 유의사항",
                        subTitle = "고객의 정보가 삭제되며 복구가 불가능합니다.",
                        content = """
                    * 계정 개인정보(이메일, 비밀번호)
                    * 등록하신 모든 택배 추적 정보
                                """.trimIndent(),
                        leftHandler = Pair("삭제할게요", object: OptionalClickListener
                        {
                            override fun invoke(dialog: OptionalDialog)
                            {
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.vm!!.requestSignOut(res.data as String)
                                }
                                dialog.dismiss()
                            }
                        }),
                        rightHandler = Pair("다시 생각할게요", object: OptionalClickListener
                        {
                            override fun invoke(dialog: OptionalDialog)
                            {
                                dialog.dismiss()
                            }
                        })).show(supportFragmentManager, "")
                }
            }


        })

        binding.vm!!.message.observe(this, Observer { message ->
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

            binding.vm!!.otherReason.observe(this, Observer {
                if(it.isNotEmpty())
                {
                    binding.vm!!.message.postValue(it)
                }
            })

        })
    }
}