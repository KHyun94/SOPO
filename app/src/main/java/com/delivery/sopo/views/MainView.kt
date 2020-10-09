package com.delivery.sopo.views

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.enums.LockScreenStatus
import com.delivery.sopo.extentions.launchActivitiy
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.interfaces.OnMainBackPressListener
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.UserAPI
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.util.adapters.ViewPagerAdapter
import com.delivery.sopo.util.ui_util.GeneralDialog
import com.delivery.sopo.viewmodels.MainViewModel
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.views.menus.LockScreenView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.main_view.*
import kotlinx.android.synthetic.main.tap_item.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainView : BasicView<MainViewBinding>(R.layout.main_view)
{
    private var onMainBackPressListener: OnMainBackPressListener? = null

    fun setOnBackPressListener(listenerMain: OnMainBackPressListener)
    {
        this.onMainBackPressListener = listenerMain
    }

    private val mainVm: MainViewModel by viewModel()
    private val inquiryVm: InquiryViewModel by viewModel()

    lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var pageChangeListener: ViewPager.OnPageChangeListener
    private val userRepo: UserRepo by inject()

    private var transaction: FragmentTransaction? = null

    init
    {
        TAG += "MainView"
        transaction = supportFragmentManager.beginTransaction()
        NetworkManager.initPrivateApi(id = userRepo.getEmail(), pwd = userRepo.getApiPwd())
        Log.d(TAG, "ID = ${userRepo.getEmail()}")
        Log.d(TAG, "ID = ${userRepo.getApiPwd()}")
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        updateFCMToken()
        init()
    }

    private fun init()
    {
        viewpagerSetting()
        tabLayoutSetting()
    }

    private fun tabSetting(v: View)
    {
        // layout을 dynamic 처리해서 넣도록 수정
        (v as TabLayout).getTabAt(0)!!.setCustomView(R.layout.tap_item)
        v.getTabAt(0)!!.customView!!.iv_tab.setBackgroundResource(R.drawable.ic_clicked_tap_register)

        v.getTabAt(1)!!.setCustomView(R.layout.tap_item)
        v.getTabAt(1)!!.customView!!.iv_tab.setBackgroundResource(R.drawable.ic_non_clicked_tap_lookup)

        v.getTabAt(2)!!.setCustomView(R.layout.tap_item)
        v.getTabAt(2)!!.customView!!.iv_tab.setBackgroundResource(R.drawable.ic_non_clicked_tap_my)
    }

    private fun tabLayoutSetting()
    {
        val tb = tablayout_bottom_tab

        tb.run {
            setupWithViewPager(vp_main)
            tabSetting(this)
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener
            {
                override fun onTabSelected(tab: TabLayout.Tab?)
                {
                    val res = when (tab!!.position)
                    {
                        0 -> R.drawable.ic_clicked_tap_register
                        1 -> R.drawable.ic_clicked_tap_lookup
                        2 -> R.drawable.ic_clicked_tap_my
                        else -> R.drawable.ic_clicked_tap_register
                    }

                    tab.customView!!.iv_tab.setBackgroundResource(res)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?)
                {
                    val res = when (tab!!.position)
                    {
                        0 -> R.drawable.ic_non_clicked_tap_register
                        1 -> R.drawable.ic_non_clicked_tap_lookup
                        2 -> R.drawable.ic_non_clicked_tap_my
                        else -> R.drawable.ic_non_clicked_tap_register
                    }

                    tab.customView!!.iv_tab.setBackgroundResource(res)
                }

                override fun onTabReselected(tab: TabLayout.Tab?)
                {
                }
            })
        }
    }

    private fun viewpagerSetting()
    {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 3)
        pageChangeListener = object : ViewPager.OnPageChangeListener
        {

            override fun onPageScrollStateChanged(p0: Int)
            {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int)
            {
            }

            override fun onPageSelected(p0: Int)
            {
                // 0923 kh 등록 성공
                if (p0 == 1 && isRegister)
                {
                    onCompleteRegister()
                    isRegister = false
                }
            }
        }

        vp_main.adapter = viewPagerAdapter
        vp_main.addOnPageChangeListener(pageChangeListener)
    }

    private fun updateFCMToken()
    {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->

            val token = task.result!!.token

            Log.d(TAG, "FCM - $token")

            NetworkManager.privateRetro.create(UserAPI::class.java)
                .updateFCMToken(userRepo.getEmail(), token)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(io())
                .subscribe(
                    {
                        Log.d(TAG, it)
                    },
                    {
                        Log.d(TAG, it.message)
                    }
                )
        }
    }

    override fun bindView()
    {
        binding.vm = mainVm
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
        mainVm.isSetOfSecurity.observe(this, Observer {
            it?.also {
                this.launchActivitiy<LockScreenView>{
                    putExtra(IntentConst.LOCK_SCREEN, LockScreenStatus.VERIFY)
                }
            }
        })

        mainVm.tabLayoutVisibility.observe(this, Observer {
            when (it)
            {
                View.VISIBLE -> tablayout_bottom_tab.visibility = View.VISIBLE
                View.INVISIBLE -> tablayout_bottom_tab.visibility = View.INVISIBLE
                View.GONE -> tablayout_bottom_tab.visibility = View.GONE
            }
        })

        mainVm.errorMsg.observe(this, Observer {
            if (it != null && it.isNotEmpty())
            {
                GeneralDialog(
                    act = this@MainView,
                    title = "에러",
                    msg = it,
                    detailMsg = null,
                    rHandler = Pair(
                        first = "네",
                        second = { it ->
                            userRepo.removeUserRepo()
                            it.dismiss()
                            finish()
                        })
                ).show(supportFragmentManager, "tag")
            }
        })
    }

    override fun onBackPressed()
    {
        onMainBackPressListener!!.onBackPressed()
//        super.onBackPressed()
    }

    fun onCompleteRegister()
    {
        isRegister = true
        binding.vpMain.currentItem = 1
        inquiryVm.refreshOngoing()
    }

    override fun onResume()
    {
        super.onResume()

    }

    companion object
    {
        var isRegister = false
    }
}