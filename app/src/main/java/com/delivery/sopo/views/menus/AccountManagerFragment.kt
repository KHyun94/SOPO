package com.delivery.sopo.views.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentAccountManagerBinding
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.viewmodels.menus.AccountManagerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class AccountManagerFragment: Fragment()
{
    lateinit var binding: FragmentAccountManagerBinding
    private val vm: AccountManagerViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_account_manager, container, false)
        bindView(view)
        setObserve()
        return binding.root
    }

    fun bindView(v: View)
    {
        binding = FragmentAccountManagerBinding.bind(v)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    fun setObserve()
    {
        vm.navigator.observe(this, Observer { menu ->
            when(menu)
            {
                MenuEnum.UPDATE_NICKNAME ->
                {
                    Toast.makeText(context, "닉네임 업데이트", Toast.LENGTH_LONG).show()
                }
                MenuEnum.SIGN_OUT ->
                {
                    Toast.makeText(context, "계정탈퇴", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    companion object{
        fun newInstance() = AccountManagerFragment()
    }
}