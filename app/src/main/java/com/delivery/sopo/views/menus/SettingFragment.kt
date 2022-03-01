package com.delivery.sopo.views.menus

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentSettingBinding
import com.delivery.sopo.databinding.ItemTimeTabBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.launchActivitiy
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.viewmodels.menus.SettingViewModel
import com.delivery.sopo.views.dialog.SelectNotifyKindDialog
import com.delivery.sopo.views.main.MainView
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs


class SettingFragment: BaseFragment<FragmentSettingBinding, SettingViewModel>()
{
    override val vm: SettingViewModel by viewModel()
    override val layoutRes: Int = R.layout.fragment_setting
    override val mainLayout: View by lazy { binding.constraintMainSetting }
    private val parentView: MainView by lazy { activity as MainView }

    lateinit var startTabBinding: ItemTimeTabBinding
    lateinit var endTabBinding: ItemTimeTabBinding

    fun setTab()
    {
        startTabBinding = DataBindingUtil.setContentView(requireActivity(), R.layout.item_time_tab)
        startTabBinding.tvTitle.text = "시작"
        endTabBinding = DataBindingUtil.setContentView(requireActivity(), R.layout.item_time_tab)
        endTabBinding.tvTitle.text = "끝"
    }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        useCommonBackPressListener(isUseCommon = true)

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent(true)
        {
            override fun onBackPressed()
            {
                super.onBackPressed()
                TabCode.MY_MENU_MAIN.FRAGMENT = MenuFragment.newInstance()
                FragmentManager.move(requireActivity(), TabCode.MY_MENU_MAIN, MenuMainFragment.viewId)
            }
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        test()

        parentView.hideTab()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {

            binding.slideMainSetting.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }, 300)


    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return
        parentView.currentPage.observe(this) {
            if(it != 2) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(requireActivity(), Observer { navigator ->
            SopoLog.d("navigator[$navigator]")
            when(navigator)
            {
                NavigatorConst.TO_NOT_DISTURB ->
                {
                    val intent = Intent(parentView, NotDisturbTimeView::class.java)
                    startActivity(intent)
                }
                NavigatorConst.TO_SET_NOTIFY_OPTION ->
                {
                    SelectNotifyKindDialog().show(requireActivity().supportFragmentManager, "SelectNotifyKindDialog")
                }
                NavigatorConst.TO_UPDATE_APP_PASSWORD ->
                {
                    activity?.launchActivitiy<LockScreenView> {
                        putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.SET)
                    }
                }
            }
        })

        vm.showSetPassword.observe(requireActivity(), Observer {
            if(it)
            {
                activity?.launchActivitiy<LockScreenView> {
                    putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.SET)
                }
            }
            else
            {
            }
        })
    }

    var startTimeList : List<String> = "00:00".split(":")
    var endTimeList : List<String> = "00:00".split(":")

    fun test()
    {

//        setTab()
        val tabs = binding.tabs
        tabs.addTab(tabs.newTab().setCustomView(R.layout.item_time_tab))
        tabs.addTab(tabs.newTab().setCustomView(R.layout.item_time_tab))

        startTabBinding = ItemTimeTabBinding.bind(tabs.getTabAt(0)?.customView ?: return)
        startTabBinding.tvTitle.text = "시작"
        endTabBinding = ItemTimeTabBinding.bind(tabs.getTabAt(1)?.customView ?: return)
        endTabBinding.tvTitle.text = "종료"
//        tabs.addTab(tabs.newTab().setText("시작\n11:00"))
//        tabs.addTab(tabs.newTab().setText("종료"))

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener
                                      {
                                          override fun onTabSelected(tab: TabLayout.Tab?)
                                          {
                                              when(tab?.position){
                                                  0 ->
                                                  {
                                                      startTabBinding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))
                                                      startTabBinding.tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))

                                                      binding.constraintStart.visibility = TabLayout.VISIBLE

                                                  }
                                                  1 ->
                                                  {
                                                      endTabBinding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))
                                                      endTabBinding.tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))

                                                      binding.constraintEnd.visibility = TabLayout.VISIBLE
                                                  }
                                              }

                                          }

                                          override fun onTabUnselected(tab: TabLayout.Tab?)
                                          {
                                              when(tab?.position){
                                                  0 ->
                                                  {
                                                      startTabBinding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
                                                      startTabBinding.tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_200))

                                                      binding.constraintEnd.visibility = TabLayout.INVISIBLE
                                                  }
                                                  1 ->
                                                  {
                                                      endTabBinding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
                                                      endTabBinding.tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_200))

                                                      binding.constraintStart.visibility = TabLayout.INVISIBLE
                                                  }
                                              }

                                          }

                                          override fun onTabReselected(tab: TabLayout.Tab?)
                                          {
                                          }

                                      })

        binding.datePickerStart.setIs24HourView(true)
        binding.datePickerEnd.setIs24HourView(true)

        binding.datePickerStart.setOnTimeChangedListener { view, hourOfDay, minute ->
            startTabBinding.tvTime.text = "${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        }

        binding.datePickerEnd.setOnTimeChangedListener { view, hourOfDay, minute ->
            endTabBinding.tvTime.text = "${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        }

        binding.datePickerStart.hour = startTimeList[0].toInt()
        binding.datePickerStart.minute = startTimeList[1].toInt()

        binding.datePickerEnd.hour = endTimeList[0].toInt()
        binding.datePickerEnd.minute = endTimeList[1].toInt()

        setClickEvent()
    }

    private fun setClickEvent(){

        binding.tvOkBtn.setOnClickListener {
            SopoLog.d( msg = "Ok button")

            val startHour = binding.datePickerStart.hour
            val startMin = binding.datePickerStart.minute
            val endHour = binding.datePickerEnd.hour
            val endMin = binding.datePickerEnd.minute

            val toStartTotalMin = startHour * 60 + startMin
            val toEndTotalMin = endHour * 60 + endMin

            val abs = abs(toStartTotalMin - toEndTotalMin)

            var startTime = "00:00"
            var endTime = "00:00"

            if(abs <= 90)
            {
                Toast.makeText(SOPOApp.INSTANCE, "방해 금지 시간대는 최소 한시간 반이상이어야 합니다.", Toast.LENGTH_LONG).show()
                startTime = "00:00"
                endTime = "00:00"
            }
            else
            {
                startTime = "${startHour.toString().padStart(2, '0')}:${startMin.toString().padStart(2, '0')}"
                endTime = "${endHour.toString().padStart(2, '0')}:${endMin.toString().padStart(2, '0')}"
            }
        }
    }

    companion object
    {
        fun newInstance(): SettingFragment
        {
            return SettingFragment()
        }
    }
}