package com.delivery.sopo.views.menus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentAccountManagerBinding
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.AccountManagerViewModel
import com.delivery.sopo.viewmodels.menus.MenuMainFrame
import com.delivery.sopo.views.login.ResetPasswordView
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.function.Function


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
        vm.navigator.observe(this, Observer { navigator ->
            when(navigator)
            {
                NavigatorConst.TO_UPDATE_NICKNAME ->
                {
                    Intent(this.requireContext(), UpdateNicknameView::class.java).launchActivity(
                        this.requireContext())
                }
                NavigatorConst.TO_RESET_PASSWORD ->
                {
                    Intent(this.requireContext(), ResetPasswordView::class.java).launchActivity(
                        this.requireContext())
                }
                NavigatorConst.TO_LOGOUT ->
                {
                    /**
                     * TODO 전체 테이블 clear
                     */
                }
                NavigatorConst.TO_SIGN_OUT ->
                {
                    Intent(this.requireContext(), SignOutView::class.java).launchActivity(
                        this.requireContext())
                }
            }
        })
    }

    companion object
    {
        fun newInstance() = AccountManagerFragment()
    }
}