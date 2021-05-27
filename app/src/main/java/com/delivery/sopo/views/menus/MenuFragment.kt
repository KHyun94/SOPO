package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.R
import com.delivery.sopo.databinding.MenuViewBinding
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.data.repository.local.repository.ParcelRepoImpl
import com.delivery.sopo.data.repository.local.repository.TimeCountRepoImpl
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.factory.MenuViewModelFactory
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import java.util.function.Function


class MenuFragment : Fragment()
{
    private val userLocalRepository: UserLocalRepository by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()
    private val timeCountRepoImpl: TimeCountRepoImpl by inject()
    private val menuVm: MenuViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            MenuViewModelFactory(userLocalRepository, parcelRepoImpl, timeCountRepoImpl)
        ).get(MenuViewModel::class.java)
    }

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

        viewId = binding.frameMenu.id
        viewBinding()
        setObserver()

        return binding.root
    }


    private fun viewBinding()
    {
        binding.vm = menuVm
        binding.lifecycleOwner = this
    }

    fun setObserver()
    {
        var pressedTime: Long = 0

        parentView.currentPage.observe(this, Observer {
            if (it != null && it == 2)
            {
                callback = object : OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        SopoLog.d( msg = "MenuFragment:: BackPressListener")

                        isMainMenu = binding.constraintFragmentBase.visibility == View.GONE

                        if (isMainMenu)
                        {
                            if (System.currentTimeMillis() - pressedTime > 2000)
                            {
                                pressedTime = System.currentTimeMillis()
                                val snackbar = Snackbar.make(
                                    parentView.binding.layoutMain,
                                    "한번 더 누르시면 앱이 종료됩니다.",
                                    2000
                                )
                                snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()

                                SopoLog.d("MenuFragment::1 BackPressListener = 종료를 위해 한번 더 클릭")
                            }
                            else
                            {
                                SopoLog.d("MenuFragment::1 BackPressListener = 종료")
                                ActivityCompat.finishAffinity(activity!!)
                                System.exit(0)
                            }
                        }
                        else
                        {
                            SopoLog.d( msg = "Sub MenuFragment:: BackPressListener")
                            binding.vm!!.popView()
                        }
                    }
                }

                requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
            }
        })

        menuVm.menu.observe(this, Observer { enum ->
            when (enum)
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
                    move(menuView, AppInfoFragment(), 0)
                }
                MenuEnum.APP_INFO ->
                {
                    move(menuView, AppInfoFragment(), 0)
                }
                MenuEnum.NOT_DISTURB ->
                {
                    move(menuView, NotDisturbTimeFragment(), 0)
                }
                MenuEnum.ACCOUNT_MANAGER ->
                {
                    move(menuView, AccountManagerFragment(), 0)
                }
                MenuEnum.SIGN_OUT ->
                {
                    move(menuView, SignOutView(), 0)
                }
                MenuEnum.UPDATE_NICKNAME ->
                {
//                    move(menuView, UpdateNicknameView(), 0)
                }
            }
        })

        binding.vm!!.isUpdate.observe(this, Observer {
            if (it)
            {
                val edit = MutableLiveData<String>()

                AlertUtil.updateValueDialog(
                    context!!,
                    "사용하실 닉네임을 입력해주세요.",
                    Pair("확인", View.OnClickListener {
                        edit.observe(this@MenuFragment, Observer {
                            SopoLog.d(msg = "입력 값 = > $it")
                            binding.vm!!.updateUserNickname(it)
                            AlertUtil.onDismiss()
                        })
                    }),
                    Pair("취소", null),
                    Function {
                        edit.value = it
                    })

                binding.vm!!.isUpdate.value = false
            }
        })
    }

    private fun move(activity: FragmentActivity, fragment: Fragment, animation: Int)
    {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        transaction.replace(R.id.frame_menu, fragment).commitAllowingStateLoss()
    }

    var callback: OnBackPressedCallback? = null

    override fun onDetach()
    {
        super.onDetach()
        if (callback != null) callback!!.remove()
    }

    companion object{
        var viewId = 0
    }
}