package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.R
import com.delivery.sopo.databinding.MenuViewBinding
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.viewmodels.factory.MenuViewModelFactory
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import kotlinx.android.synthetic.main.menu_view.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MenuFragment : Fragment(){

    private val userRepo: UserRepo by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()
    private val timeCountRepoImpl: TimeCountRepoImpl by inject()
    private val menuVm: MenuViewModel by lazy {
        ViewModelProvider(requireActivity(), MenuViewModelFactory(userRepo, parcelRepoImpl, timeCountRepoImpl)).get(MenuViewModel::class.java)
    }
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    private lateinit var menuView: FragmentActivity
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
        setListener()

        menuView = this.requireActivity()

        lifecycle.addObserver(menuVm)

        return binding.root
    }

    private fun viewBinding() {
        binding.vm = menuVm
        binding.lifecycleOwner = this
        binding.executePendingBindings() // 즉 바인딩
    }

    fun setObserver(){
        menuVm.menu.observe(this, Observer {
            when(it){
                MenuEnum.NOTICE-> {
                    move(menuView, NoticeFragment(), 0)
                }
                MenuEnum.SETTING -> {
                    move(menuView, SettingFragment(), 0)
                }
                MenuEnum.FAQ -> {
                    move(menuView, FaqFragment(), 0)
                }
                MenuEnum.USE_TERMS -> {
                    move(menuView, NotDisturbTimeFragment(), 0)
                }
                MenuEnum.APP_INFO -> {
                    move(menuView, AppInfoFragment(), 0)
                }
                MenuEnum.NOT_DISTURB -> {
                    move(menuView, NotDisturbTimeFragment(), 0)
                }
                else -> {
                }
            }
        })
    }

    private fun move(activity: FragmentActivity, fragment: Fragment, animation: Int) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_menu, fragment).commitAllowingStateLoss()
    }

    private fun setListener(){
        binding.root.relative_profile.setOnClickListener {

        }
    }
}