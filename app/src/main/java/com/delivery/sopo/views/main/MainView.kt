package com.delivery.sopo.views.main

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.extensions.launchActivitiy
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.PowerManager
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.viewmodels.main.MainViewModel
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.menus.LockScreenView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.main_view.*
import kotlinx.android.synthetic.main.tap_item.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainView : BasicView<MainViewBinding>(R.layout.main_view)
{
    private val mainVm: MainViewModel by viewModel()
    private val inquiryVm: InquiryViewModel by viewModel()

    private val userRepoImpl: UserRepoImpl by inject()
    val appDatabase: AppDatabase by inject()

    lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var pageChangeListener: ViewPager.OnPageChangeListener

    var currentPage = MutableLiveData<Int?>()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding.vm!!.adapter.value = ViewPagerAdapter(supportFragmentManager, 3)
        initUI()
        PowerManager.checkWhiteList(this)
    }

    override fun bindView()
    {
        binding.vm = mainVm
    }

    override fun setObserver()
    {
        /** 화면 패스워드 설정 **/
        binding.vm!!.isSetOfSecurity.observe(this, Observer {
            it?.also {
                this.launchActivitiy<LockScreenView> {
                    putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.VERIFY)
                }
            }
        })

        binding.vm!!.tabLayoutVisibility.observe(this, Observer {

            binding.tlMain.run {
                when (it)
                {
                    View.VISIBLE -> visibility = View.VISIBLE
                    View.INVISIBLE -> visibility = View.INVISIBLE
                    View.GONE -> visibility = View.GONE
                }
            }
        })

        binding.vm!!.errorMsg.observe(this, Observer {
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
                            userRepoImpl.removeUserRepo()
                            it.dismiss()
                            finish()
                        })
                ).show(supportFragmentManager, "tag")
            }
        })

        // todo 업데이트 시
        SOPOApp.cntOfBeUpdate.observeForever {
            if(it == null) return@observeForever

            SopoLog.d(msg = "업데이트 가능 여부 택배 갯수 ${it}")

            if (binding.vm!!.isInitUpdate && it > 0)
            {
                SopoLog.d(msg = "True 업데이트 가능 여부 택배 갯수: $it")

                binding.alertMessageBar.run {
                    setText("${it}개의 새로운 배송정보가 있어요.")
                    setTextColor(R.color.MAIN_WHITE)
                    setOnCancelClicked("업데이트", R.color.MAIN_WHITE, View.OnClickListener {
                        when (currentPage.value)
                        {
                            NavigatorConst.REGISTER_TAB ->
                            {
                                SopoLog.d("Click For Update at RegisterTab")
                                binding.vpMain.currentItem = 1

                            }
                            NavigatorConst.INQUIRY_TAB ->
                            {
                                SopoLog.d("Click For Update at InquiryTab")

                            }
                            NavigatorConst.MY_MENU_TAB ->
                            {
                                SopoLog.d("Click For Update at MenuTab")
                                binding.vpMain.currentItem = 1

                            }
                        }

                        inquiryVm.refreshOngoing()

                        this.onDismiss()
                    })
                    onStart(null)
                    SOPOApp.cntOfBeUpdate.postValue(0)
                }
            }
        }

        SOPOApp.currentPage.observe(this, Observer {
            if (it != null)
            {
                when (it)
                {
                    NavigatorConst.REGISTER_TAB -> binding.vpMain.setCurrentItem(
                        NavigatorConst.REGISTER_TAB,
                        true
                    )
                    NavigatorConst.INQUIRY_TAB -> binding.vpMain.setCurrentItem(
                        NavigatorConst.INQUIRY_TAB,
                        true
                    )
                    else -> binding.vpMain.setCurrentItem(0, true)
                }

            }
        })
    }

    fun getAlertMessageBar() = binding.alertMessageBar

    fun onCompleteRegister()
    {
        isRegister = true
        binding.vpMain.currentItem = 1
        inquiryVm.refreshOngoing()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        OtherUtil.clearCache(SOPOApp.INSTANCE)
    }

    private fun initUI()
    {
        viewpagerSetting()
        tabLayoutSetting()
    }

    private fun tabSetting(v: TabLayout)
    {
        // layout을 dynamic 처리해서 넣도록 수정
        v.getTabAt(NavigatorConst.REGISTER_TAB)!!.run {
            setCustomView(R.layout.tap_item)
            customView!!.run {
                iv_tab.setBackgroundResource(R.drawable.ic_activate_register)
                tv_tab_name.setText("등록")
                tv_tab_name.setTextColor(resources.getColor(R.color.COLOR_MAIN_700))
            }
        }

        v.getTabAt(NavigatorConst.INQUIRY_TAB)!!.run {
            setCustomView(R.layout.tap_item)
            customView!!.run {
                iv_tab.setBackgroundResource(R.drawable.ic_inactivate_inquiry)
                tv_tab_name.setText("조회")
                tv_tab_name.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))
            }
        }

        v.getTabAt(NavigatorConst.MY_MENU_TAB)!!.run {
            setCustomView(R.layout.tap_item)
            customView!!.run {
                iv_tab.setBackgroundResource(R.drawable.ic_inactivate_menu)
                tv_tab_name.setText("메뉴")
                tv_tab_name.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))
            }
        }
    }

    private fun tabLayoutSetting()
    {
        val tb = binding.tlMain

        tb.run {
            setupWithViewPager(vp_main)
            tabSetting(this)
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener
            {
                override fun onTabSelected(tab: TabLayout.Tab?)
                {
                    currentPage.postValue(tab!!.position)

                    val res = when (tab.position)
                    {
                        NavigatorConst.REGISTER_TAB -> R.drawable.ic_activate_register
                        NavigatorConst.INQUIRY_TAB -> R.drawable.ic_activate_inquiry
                        NavigatorConst.MY_MENU_TAB -> R.drawable.ic_activate_menu
                        else -> NavigatorConst.REGISTER_TAB
                    }

                    tab.customView!!.iv_tab.setBackgroundResource(res)
                    tab.customView!!.tv_tab_name.setTextColor(resources.getColor(R.color.COLOR_MAIN_700))
                }

                override fun onTabUnselected(tab: TabLayout.Tab?)
                {
                    val res = when (tab!!.position)
                    {
                        NavigatorConst.REGISTER_TAB -> R.drawable.ic_inactivate_register
                        NavigatorConst.INQUIRY_TAB -> R.drawable.ic_inactivate_inquiry
                        NavigatorConst.MY_MENU_TAB -> R.drawable.ic_inactivate_menu
                        else -> NavigatorConst.REGISTER_TAB
                    }

                    tab.customView!!.iv_tab.setBackgroundResource(res)
                    tab.customView!!.tv_tab_name.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))
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
                    SopoLog.d( msg = "등록 성공 메인 뷰")
                    onCompleteRegister()
                    isRegister = false
                }
            }
        }

        vp_main.adapter = viewPagerAdapter
        vp_main.addOnPageChangeListener(pageChangeListener)

    }


    companion object
    {
        var isRegister = false
    }
}