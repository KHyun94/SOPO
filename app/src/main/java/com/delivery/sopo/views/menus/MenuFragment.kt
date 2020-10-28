package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.R
import com.delivery.sopo.databinding.MenuViewBinding
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.viewmodels.factory.MenuViewModelFactory
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.views.main.MainView
import kotlinx.android.synthetic.main.menu_view.view.*
import org.koin.android.ext.android.inject


class MenuFragment : Fragment()
{

    private val userRepoImpl: UserRepoImpl by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()
    private val timeCountRepoImpl: TimeCountRepoImpl by inject()
    private val menuVm: MenuViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            MenuViewModelFactory(userRepoImpl, parcelRepoImpl, timeCountRepoImpl)
        ).get(MenuViewModel::class.java)
    }
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    private lateinit var menuView: FragmentActivity
    private lateinit var binding: MenuViewBinding
    private lateinit var parentView: MainView

    private var isMainMenu = true

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = MenuViewBinding.inflate(inflater, container, false)
        menuView = this.requireActivity()
        parentView = activity as MainView

        viewBinding()
        setObserver()
        setListener()

        return binding.root
    }


    private fun viewBinding()
    {
        binding.vm = menuVm
        binding.lifecycleOwner = this
        binding.executePendingBindings() // 즉 바인딩
    }

    fun setObserver()
    {
        parentView.currentPage.observe(this, Observer {
            if (it != null && it == 2)
            {
                callback = object : OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        Log.d(TAG, "MenuFragment:: BackPressListener")

                        isMainMenu = binding.constraintFragmentBase.visibility == View.GONE

                        if(isMainMenu)
                        {
                            Log.d(TAG, "Main MenuFragment:: BackPressListener")

                            ActivityCompat.finishAffinity(activity!!)
                            System.exit(0)
                        }
                        else
                        {
                            Log.d(TAG, "Sub MenuFragment:: BackPressListener")
                            binding.vm!!.popView()
                        }

                    }

                }

                requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
            }
        })

        menuVm.menu.observe(this, Observer {

            when (it)
            {
                MenuEnum.NOTICE ->
                {
                    move(menuView, NoticeFragment(), 0)
                }
                MenuEnum.SETTING ->
                {
                    move(menuView, SettingFragment(), 0)
                }
                MenuEnum.FAQ ->
                {
                    move(menuView, FaqFragment(), 0)
                }
                MenuEnum.USE_TERMS ->
                {
                    move(menuView, NotDisturbTimeFragment(), 0)
                }
                MenuEnum.APP_INFO ->
                {
                    move(menuView, AppInfoFragment(), 0)
                }
                MenuEnum.NOT_DISTURB ->
                {
                    move(menuView, NotDisturbTimeFragment(), 0)
                }
                else ->
                {
                }
            }
        })
    }

    private fun move(activity: FragmentActivity, fragment: Fragment, animation: Int)
    {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_menu, fragment).commitAllowingStateLoss()
    }

    private fun setListener()
    {
        binding.root.relative_profile.setOnClickListener {

        }

    }

    var callback: OnBackPressedCallback? = null

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if(isMainMenu)
                {
                    ActivityCompat.finishAffinity(activity!!)
                    System.exit(0)
                }
                else
                {
                    binding.vm!!.popView()
                }
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
    }

    override fun onDetach()
    {
        super.onDetach()

        callback!!.remove()
    }
}