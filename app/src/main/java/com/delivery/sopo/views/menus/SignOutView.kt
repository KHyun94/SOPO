package com.delivery.sopo.views.menus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ConfirmDeleteDialogBinding
import com.delivery.sopo.databinding.SignOutViewBinding
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.viewmodels.menus.SignOutViewModel
import com.delivery.sopo.views.dialog.ConfirmDeleteDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignOutView: Fragment()
{
    lateinit var binding: SignOutViewBinding
    private val vm: SignOutViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.sign_out_view, container, false)
        bindView(view)
        setObserve()
        return binding.root
    }

    fun bindView(v: View)
    {
        binding = SignOutViewBinding.bind(v)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    fun setObserve()
    {
        binding.vm!!.result.observe(this, Observer { res ->

            val dialog = ConfirmDeleteDialog(
                requireActivity(), 0, "탈퇴 시 유의사항", "고객의 정보가 삭제되며 복구가 불가능합니다.", """
* 계정 개인정보(이메일, 비밀번호)
* 등록하신 모든 택배 추적 정보
            """.trimIndent(), Pair("탈퇴하기", object: ((ConfirmDeleteDialog) -> Unit)
                {
                    override fun invoke(dialog: ConfirmDeleteDialog)
                    {
                        Toast.makeText(context, "선택 메시지 >>> $res", Toast.LENGTH_LONG).show()
                    }
                })
            )

            dialog.show(activity?.supportFragmentManager!!, "")
        })
    }
}