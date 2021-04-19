package com.delivery.sopo.views.menus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delivery.sopo.R
import com.delivery.sopo.databinding.SignOutViewBinding
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.viewmodels.signup.SignOutViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignOutView: Fragment()
{
    lateinit var binding: SignOutViewBinding
    private val vm: SignOutViewModel by viewModel()
    private val menuVm: MenuViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.sign_out_view, container, false)
        bindView(view)
        return binding.root
    }

    fun bindView(v: View)
    {
        binding = SignOutViewBinding.bind(v)
        binding.vm = vm
        binding.lifecycleOwner = this
    }
}