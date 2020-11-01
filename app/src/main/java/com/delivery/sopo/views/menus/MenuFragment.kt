package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.factory.MenuViewModelFactory
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.menu_view.view.*
import org.koin.android.ext.android.inject
import java.util.function.Function


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
        var pressedTime: Long = 0

        parentView.currentPage.observe(this, Observer {
            if (it != null && it == 2)
            {
                callback = object : OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        Log.d(TAG, "MenuFragment:: BackPressListener")

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

                                SopoLog.d(
                                    "MenuFragment::1 BackPressListener = 종료를 위해 한번 더 클릭",
                                    null
                                )
                            }
                            else
                            {
                                SopoLog.d("MenuFragment::1 BackPressListener = 종료", null)
                                ActivityCompat.finishAffinity(activity!!)
                                System.exit(0)
                            }
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

        // todo nickname 업데이트 api 줘...
        binding.vm!!.isUpdate.observe(this, Observer {
            if (it)
            {
                val edit = MutableLiveData<String>()

                AlertUtil.updateValueDialog(
                    context!!,
                    "사용하실 닉네임을 입력해주세요.",
                    Pair("확인", View.OnClickListener {
                        edit.observe(this@MenuFragment, Observer {
                            SopoLog.d("입력 값 = > $it")
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
        transaction.replace(R.id.frame_menu, fragment).commitAllowingStateLoss()
    }

    private fun setListener()
    {
        binding.root.relative_profile.setOnClickListener {

        }

    }

    var callback: OnBackPressedCallback? = null


    override fun onDetach()
    {
        super.onDetach()
        if (callback != null) callback!!.remove()
    }
}