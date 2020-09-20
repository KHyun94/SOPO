package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.delivery.sopo.databinding.MenuViewBinding
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class MenuFragment : Fragment(){

    private val menuVM: MenuViewModel by viewModel()
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    private lateinit var binding: MenuViewBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MenuViewBinding.inflate(inflater, container, false)
        viewBinding()
        setObserver()

        lifecycle.addObserver(menuVM)

        return binding.root
    }

    private fun viewBinding() {
        binding.vm = menuVM
        binding.lifecycleOwner = this
        binding.executePendingBindings() // 즉 바인딩
    }

    fun setObserver(){
    }
}