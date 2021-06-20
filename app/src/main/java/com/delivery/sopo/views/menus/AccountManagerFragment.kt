package com.delivery.sopo.views.menus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentAccountManagerBinding
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.data.repository.local.repository.ParcelLocalRepository
import com.delivery.sopo.data.repository.local.repository.TimeCountRepoImpl
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.viewmodels.factory.MenuViewModelFactory
import com.delivery.sopo.viewmodels.menus.AccountManagerViewModel
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class AccountManagerFragment: Fragment()
{
    lateinit var binding: FragmentAccountManagerBinding
    private val vm: AccountManagerViewModel by viewModel()

    private val userLocalRepository: UserLocalRepository by inject()
    private val parcelLocalRepository: ParcelLocalRepository by inject()
    private val timeCountRepoImpl: TimeCountRepoImpl by inject()

    private val menuVm: MenuViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            MenuViewModelFactory(userLocalRepository)
        ).get(MenuViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

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
        binding.vm!!.navigator.observe(this, Observer { menu ->
            when(menu)
            {
                MenuEnum.UPDATE_NICKNAME ->
                {
                    Toast.makeText(context, "닉네임 업데이트", Toast.LENGTH_LONG).show()
//                    menuVm.pushView(menu)
                }
                MenuEnum.SIGN_OUT ->
                {
                    Toast.makeText(context, "계정탈퇴", Toast.LENGTH_LONG).show()
//                    menuVm.pushView(menu)
                }
            }
        })
    }
}