package com.delivery.sopo.views.signup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.databinding.SignUpStep2ViewBinding
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.viewmodels.signup.UpdateNicknameViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.widget.CustomEditText
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdateNicknameView: AppCompatActivity()
{
    private lateinit var binding: SignUpStep2ViewBinding
    private val vm: UpdateNicknameViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<SignUpStep2ViewBinding>(this, R.layout.sign_up_step2_view)
        binding.vm = vm
        binding.lifecycleOwner = this

        setObserve()
    }

    private fun setObserve()
    {
        binding.vm!!.result.observe(this@UpdateNicknameView, Observer {result ->

            if(!result.result)
            {
                SopoLog.d("실패 닉네임 업데이트 여부 확인 ${result.result}, ${result.code}, ${result.message}")

                GeneralDialog(this@UpdateNicknameView,"오류", "닉네임 등록이 실패했습니다.\n다시 시도해주세요.", null, Pair("네",null)).show(supportFragmentManager, "DIALOG")
                return@Observer
            }

            SopoLog.d("성공 닉네임 업데이트 여부 확인 ${result.result}, ${result.code}, ${result.message}")

            GeneralDialog(this@UpdateNicknameView, "성공", "정상적으로 닉네임을 등록했습니다.", null,
            Pair("네", object : OnAgreeClickListener{ override fun invoke(agree: GeneralDialog) {
                    val intent = Intent(this@UpdateNicknameView, MainView::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
            })).show(supportFragmentManager, "DIALOG")
        })

        binding.vm!!.nickname.observe(this@UpdateNicknameView, Observer { nickname ->

            if(nickname.isEmpty())
            {
                binding.vm!!.setVisibleState(type = InfoConst.NICKNAME, errorState = View.GONE, corState = View.GONE)
                binding.vm!!.statusType.value = CustomEditText.STATUS_COLOR_ELSE
                return@Observer
            }

            if(ValidateUtil.isValidateNickname(nickname))
            {
                binding.vm!!.setVisibleState(type = InfoConst.NICKNAME, errorState = View.GONE, corState = View.VISIBLE)
                binding.vm!!.statusType.value = CustomEditText.STATUS_COLOR_BLUE

                return@Observer
            }

            binding.vm!!.setVisibleState(type = InfoConst.NICKNAME, errorState = View.VISIBLE, corState = View.GONE)
            binding.vm!!.statusType.value = CustomEditText.STATUS_COLOR_RED
        })
    }
}