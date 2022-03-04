package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.delivery.sopo.extensions.*
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
        endTabBinding = DataBindingUtil.setContentView(requireActivity(), R.layout.item_time_tab)
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

    fun setSelectItemView(vararg selectedItem: Pair<TextView, ImageView>)
    {
        selectedItem.forEach { item ->
            item.first.convertTextColor(R.color.COLOR_GRAY_800)
            item.second.makeVisible()
        }
    }

    fun setUnselectItemView(vararg unselectedItem: Pair<TextView, ImageView>)
    {
        unselectedItem.forEach { item ->
            item.first.convertTextColor(R.color.COLOR_GRAY_500)
            item.second.makeGone()
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        test()

        binding.includePushAlarm.setOnItemClickListener {
            when(it.id)
            {
                R.id.constraint_main_push_always ->
                {
                    SopoLog.d("Always")
                    setSelectItemView(Pair(binding.includePushAlarm.tvAlways, binding.includePushAlarm.ivAlways))
                    setUnselectItemView(Pair(binding.includePushAlarm.tvArrive, binding.includePushAlarm.ivArrive),
                                        Pair(binding.includePushAlarm.tvReject, binding.includePushAlarm.ivReject))
                }
                R.id.constraint_main_push_arrive ->
                {
                    SopoLog.d("Arrive")
                    setSelectItemView(Pair(binding.includePushAlarm.tvArrive, binding.includePushAlarm.ivArrive))
                    setUnselectItemView(Pair(binding.includePushAlarm.tvAlways, binding.includePushAlarm.ivAlways),
                                        Pair(binding.includePushAlarm.tvReject, binding.includePushAlarm.ivReject))
                }
                R.id.constraint_main_push_reject ->
                {
                    SopoLog.d("Reject")
                    setSelectItemView(Pair(binding.includePushAlarm.tvReject, binding.includePushAlarm.ivReject))
                    setUnselectItemView(
                        Pair(binding.includePushAlarm.tvArrive, binding.includePushAlarm.ivArrive),
                        Pair(binding.includePushAlarm.tvAlways, binding.includePushAlarm.ivAlways))
                }
            }
        }

        binding.includeLockApp.setOnItemClickListener {
            when(it.id)
            {
                R.id.constraint_main_set_on ->
                {
                    SopoLog.d("Always")
                    setSelectItemView(Pair(binding.includeLockApp.tvSetOn, binding.includeLockApp.ivSetOn))
                    setUnselectItemView(Pair(binding.includeLockApp.tvSetOff, binding.includeLockApp.ivSetOff))
                }
                R.id.constraint_main_set_off ->
                {
                    SopoLog.d("Arrive")
                    setSelectItemView(Pair(binding.includeLockApp.tvSetOff, binding.includeLockApp.ivSetOff))
                    setUnselectItemView(Pair(binding.includeLockApp.tvSetOn, binding.includeLockApp.ivSetOn))
                }

            }
        }

    }

    private fun setDisableView()
    {
        binding.constraintSettingNotDisturbTime.disabledClick()
        binding.constraintMainNotDisturbTime.disabledClick()
        binding.constraintMainLockApp.disabledClick()
    }

    fun setEnableView()
    {
        binding.constraintSettingNotDisturbTime.enabledClick()
        binding.constraintMainNotDisturbTime.enabledClick()
        binding.constraintMainLockApp.enabledClick()
    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return
        parentView.currentPage.observe(this) {
            if(it != 2) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        binding.slideMainSetting.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener
                                                       {
                                                           override fun onPanelSlide(panel: View?, slideOffset: Float)
                                                           {
                                                           }

                                                           override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?)
                                                           {
                                                               newState ?: return

                                                               when(newState)
                                                               {
                                                                   SlidingUpPanelLayout.PanelState.EXPANDED ->
                                                                   {
                                                                       setDisableView()
                                                                   }
                                                                   SlidingUpPanelLayout.PanelState.COLLAPSED ->
                                                                   {
                                                                       parentView.showTab()

                                                                       SopoLog.d("상태 -> $newState")
                                                                       setEnableView()
                                                                   }
                                                               }
                                                           }

                                                       })

        vm.navigator.observe(requireActivity()) { navigator ->
            SopoLog.d("navigator[$navigator]")

            when(navigator)
            {
                NavigatorConst.TO_NOT_DISTURB ->
                {
                    parentView.hideTab()

                    binding.includeLockApp.root.visibility = View.GONE
                    binding.includePushAlarm.root.visibility = View.GONE
                    binding.includeNotDisturbTime.root.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed({
                                                                    binding.slideMainSetting.panelState =
                                                                        SlidingUpPanelLayout.PanelState.EXPANDED
                                                                }, 200)
                }
                NavigatorConst.TO_SET_NOTIFY_OPTION ->
                {
                    parentView.hideTab()

                    binding.includeNotDisturbTime.root.visibility = View.GONE
                    binding.includeLockApp.root.visibility = View.GONE
                    binding.includePushAlarm.root.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed({
                                                                    binding.slideMainSetting.panelState =
                                                                        SlidingUpPanelLayout.PanelState.EXPANDED
                                                                }, 200)
                }
                NavigatorConst.TO_UPDATE_APP_PASSWORD ->
                {
                    parentView.hideTab()

                    binding.includeNotDisturbTime.root.visibility = View.GONE
                    binding.includeLockApp.root.visibility = View.GONE
                    binding.includePushAlarm.root.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        binding.slideMainSetting.panelState =
                            SlidingUpPanelLayout.PanelState.EXPANDED
                    }, 200)

                    //                    activity?.launchActivitiy<LockScreenView> {
                    //                        putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.SET)
                    //                    }
                }
            }
        }

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

    var startTimeList: List<String> = "00:00".split(":")
    var endTimeList: List<String> = "00:00".split(":")

    @SuppressLint("SetTextI18n")
    fun test()
    {
        val tabLayout = binding.includeNotDisturbTime.tabLayoutTime
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.item_time_tab))
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.item_time_tab))

        startTabBinding = ItemTimeTabBinding.bind(tabLayout.getTabAt(0)?.customView ?: return)
        endTabBinding = ItemTimeTabBinding.bind(tabLayout.getTabAt(1)?.customView ?: return)

        startTabBinding.tvTitle.text = "시작"
        endTabBinding.tvTitle.text = "종료"

        startTabBinding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))
        startTabBinding.tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))

        startTabBinding.tvTime.text = "00:00"
        endTabBinding.tvTime.text = "00:00"

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener
                                           {
                                               override fun onTabSelected(tab: TabLayout.Tab?)
                                               {
                                                   when(tab?.position)
                                                   {
                                                       0 ->
                                                       {
                                                           startTabBinding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))
                                                           startTabBinding.tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))

                                                           binding.includeNotDisturbTime.timePickerStart.visibility =
                                                               View.VISIBLE

                                                       }
                                                       1 ->
                                                       {
                                                           endTabBinding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))
                                                           endTabBinding.tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800))

                                                           binding.includeNotDisturbTime.timePickerEnd.visibility =
                                                               View.VISIBLE
                                                       }
                                                   }

                                               }

                                               override fun onTabUnselected(tab: TabLayout.Tab?)
                                               {
                                                   when(tab?.position)
                                                   {
                                                       0 ->
                                                       {
                                                           startTabBinding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
                                                           startTabBinding.tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_200))

                                                           binding.includeNotDisturbTime.timePickerStart.visibility =
                                                               View.GONE
                                                       }
                                                       1 ->
                                                       {
                                                           endTabBinding.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
                                                           endTabBinding.tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_200))

                                                           binding.includeNotDisturbTime.timePickerEnd.visibility =
                                                               View.GONE
                                                       }
                                                   }

                                               }

                                               override fun onTabReselected(tab: TabLayout.Tab?)
                                               {
                                               }

                                           })

        binding.includeNotDisturbTime.timePickerStart.setIs24HourView(true)
        binding.includeNotDisturbTime.timePickerEnd.setIs24HourView(true)

        binding.includeNotDisturbTime.timePickerStart.setOnTimeChangedListener { view, hourOfDay, minute ->
            SopoLog.d("Start TimePicker [$hourOfDay:$minute]")
            startTabBinding.tvTime.text =
                "${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padEnd(2, '0')}"
        }

        binding.includeNotDisturbTime.timePickerEnd.setOnTimeChangedListener { view, hourOfDay, minute ->
            SopoLog.d("End TimePicker [$hourOfDay:$minute]")
            endTabBinding.tvTime.text =
                "${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padEnd(2, '0')}"
        }

        setClickEvent()
    }

    private fun setClickEvent()
    {

        /* binding.includeNotDisturbTime.tvOk.setOnClickListener {
             SopoLog.d( msg = "Ok button")

             val startHour = binding.includeNotDisturbTime.timePickerStart.hour
             val startMin = binding.includeNotDisturbTime.timePickerStart.minute
             val endHour = binding.includeNotDisturbTime.timePickerEnd.hour
             val endMin = binding.includeNotDisturbTime.timePickerEnd.minute

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
         }*/
    }

    companion object
    {
        fun newInstance(): SettingFragment
        {
            return SettingFragment()
        }
    }
}