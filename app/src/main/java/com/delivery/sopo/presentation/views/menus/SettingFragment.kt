package com.delivery.sopo.presentation.views.menus

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentSettingBinding
import com.delivery.sopo.databinding.ItemTimeTabBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.SettingEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.*
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.presentation.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.presentation.viewmodels.menus.SettingViewModel
import com.delivery.sopo.presentation.views.main.MainView
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

                if(binding.slideMainSetting.panelState == SlidingUpPanelLayout.PanelState.EXPANDED)
                {
                    binding.slideMainSetting.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                    return
                }

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

    fun initNotDisturbTime()
    {
        binding.includeNotDisturbTime.run {
            timePickerStart.hour = 0
            timePickerStart.minute = 0

            timePickerEnd.hour = 0
            timePickerEnd.minute = 0

            tabLayoutTime.setScrollPosition(0, 0.0f, true, true)

            timePickerStart.makeVisible()
            timePickerEnd.makeGone()
        }

        startTabBinding.run {
            tvTitle.convertTextColor(R.color.COLOR_GRAY_800)
            tvTime.convertTextColor(R.color.COLOR_GRAY_800)
        }

        endTabBinding.run {
            tvTitle.convertTextColor(R.color.COLOR_GRAY_400)
            tvTime.convertTextColor(R.color.COLOR_GRAY_200)
        }
    }

    @SuppressLint("SetTextI18n")
    fun setNotDisturbTime()
    {
        val tabLayout = binding.includeNotDisturbTime.tabLayoutTime
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.item_time_tab))
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.item_time_tab))

        startTabBinding = ItemTimeTabBinding.bind(tabLayout.getTabAt(0)?.customView ?: return)
        endTabBinding = ItemTimeTabBinding.bind(tabLayout.getTabAt(1)?.customView ?: return)

        startTabBinding.tvTitle.text = "시작"
        endTabBinding.tvTitle.text = "종료"

        initNotDisturbTime()

        startTabBinding.tvTime.text = vm.notDisturbStartTime.value ?: "00:00"
        endTabBinding.tvTime.text = vm.notDisturbEndTime.value ?: "00:00"

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener
                                           {
                                               override fun onTabSelected(tab: TabLayout.Tab?)
                                               {
                                                   when(tab?.position)
                                                   {
                                                       0 ->
                                                       {
                                                           startTabBinding.run {
                                                               tvTitle.convertTextColor(R.color.COLOR_GRAY_800)
                                                               tvTime.convertTextColor(R.color.COLOR_GRAY_800)
                                                           }

                                                           binding.includeNotDisturbTime.timePickerStart.makeVisible()
                                                       }
                                                       1 ->
                                                       {
                                                           endTabBinding.run {
                                                               tvTitle.convertTextColor(R.color.COLOR_GRAY_800)
                                                               tvTime.convertTextColor(R.color.COLOR_GRAY_800)
                                                           }

                                                           binding.includeNotDisturbTime.timePickerEnd.makeVisible()
                                                       }
                                                   }
                                               }

                                               override fun onTabUnselected(tab: TabLayout.Tab?)
                                               {
                                                   when(tab?.position)
                                                   {
                                                       0 ->
                                                       {
                                                           startTabBinding.run {
                                                               tvTitle.convertTextColor(R.color.COLOR_GRAY_400)
                                                               tvTime.convertTextColor(R.color.COLOR_GRAY_200)
                                                           }

                                                           binding.includeNotDisturbTime.timePickerStart.makeGone()
                                                       }
                                                       1 ->
                                                       {
                                                           endTabBinding.run {
                                                               tvTitle.convertTextColor(R.color.COLOR_GRAY_400)
                                                               tvTime.convertTextColor(R.color.COLOR_GRAY_200)
                                                           }

                                                           binding.includeNotDisturbTime.timePickerEnd.makeGone()
                                                       }
                                                   }

                                               }

                                               override fun onTabReselected(tab: TabLayout.Tab?) {}
                                           })

        binding.includeNotDisturbTime.timePickerStart.setIs24HourView(true)
        binding.includeNotDisturbTime.timePickerEnd.setIs24HourView(true)

        binding.includeNotDisturbTime.timePickerStart.setOnTimeChangedListener { view, hourOfDay, minute ->
            startTabBinding.tvTime.text = "${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padEnd(2, '0')}"
        }

        binding.includeNotDisturbTime.timePickerEnd.setOnTimeChangedListener { view, hourOfDay, minute ->
            endTabBinding.tvTime.text = "${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padEnd(2, '0')}"
        }

        binding.includeNotDisturbTime.tvOk.setOnClickListener {
            val startHour = binding.includeNotDisturbTime.timePickerStart.hour
            val startMin = binding.includeNotDisturbTime.timePickerStart.minute
            val endHour = binding.includeNotDisturbTime.timePickerEnd.hour
            val endMin = binding.includeNotDisturbTime.timePickerEnd.minute

            val toStartTotalMin = startHour * 60 + startMin
            val toEndTotalMin = endHour * 60 + endMin

            val abs = abs(toStartTotalMin - toEndTotalMin)

            when
            {
                abs == 0 ->
                {
                    vm.setNotDisturbStartTime("")
                    vm.setNotDisturbEndTime("")

                    vm.setNotDisturbTime("")

                    binding.slideMainSetting.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                }
                abs <= 90 ->
                {
                    Toast.makeText(requireContext(), "방해 금지 시간대는 최소 한시간 반 이상이어야 합니다.", Toast.LENGTH_LONG)
                        .show()
                }
                else ->
                {
                    val startTime = "${startHour.toString().padStart(2, '0')}:${
                        startMin.toString()
                            .padEnd(2, '0')
                    }"
                    val endTime =
                        "${endHour.toString().padStart(2, '0')}:${endMin.toString().padEnd(2, '0')}"

                    vm.setNotDisturbStartTime(startTime)
                    vm.setNotDisturbEndTime(endTime)

                    vm.setNotDisturbTime("$startTime ~ $endTime")

                    binding.slideMainSetting.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                }
            }
        }
    }

    fun setLockPasswordListener()
    {
        var isSetOn: Boolean = false

        binding.includeLockApp.setOnItemClickListener {
            when(it.id)
            {
                R.id.constraint_main_set_on ->
                {
                    SopoLog.d("Always")
                    isSetOn = true
                    setSelectItemView(Pair(binding.includeLockApp.tvSetOn, binding.includeLockApp.ivSetOn))
                    setUnselectItemView(Pair(binding.includeLockApp.tvSetOff, binding.includeLockApp.ivSetOff))
                }
                R.id.constraint_main_set_off ->
                {
                    SopoLog.d("Arrive")
                    isSetOn = false
                    setSelectItemView(Pair(binding.includeLockApp.tvSetOff, binding.includeLockApp.ivSetOff))
                    setUnselectItemView(Pair(binding.includeLockApp.tvSetOn, binding.includeLockApp.ivSetOn))
                }
            }
        }

        binding.includeLockApp.setOnChangeClickListener {
            binding.slideMainSetting.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED

            activity?.launchActivitiy<LockScreenView> {
                putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.SET_CONFIRM)
            }
        }

        binding.includeLockApp.setOnConfirmClickListener {

            binding.slideMainSetting.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED

            if(!isSetOn)
            {
                return@setOnConfirmClickListener vm.deleteAppPassword()
            }

            if(vm.isSetOfSecurity.value?:0 > 0)
            {
                return@setOnConfirmClickListener
            }

            activity?.launchActivitiy<LockScreenView> {
                putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.SET_UPDATE)
            }
        }
    }

    fun setPushAlarmListener()
    {
        var selectedPushAlarmType: SettingEnum.PushAlarmType =
            vm.pushAlarmType.value ?: SettingEnum.PushAlarmType.ALWAYS

        binding.includePushAlarm.setOnItemClickListener {
            when(it.id)
            {
                R.id.constraint_main_push_always ->
                {
                    selectedPushAlarmType = SettingEnum.PushAlarmType.ALWAYS
                    setSelectItemView(Pair(binding.includePushAlarm.tvAlways, binding.includePushAlarm.ivAlways))
                    setUnselectItemView(Pair(binding.includePushAlarm.tvArrive, binding.includePushAlarm.ivArrive), Pair(binding.includePushAlarm.tvReject, binding.includePushAlarm.ivReject))
                }
                R.id.constraint_main_push_arrive ->
                {
                    selectedPushAlarmType = SettingEnum.PushAlarmType.ARRIVE
                    setSelectItemView(Pair(binding.includePushAlarm.tvArrive, binding.includePushAlarm.ivArrive))
                    setUnselectItemView(Pair(binding.includePushAlarm.tvAlways, binding.includePushAlarm.ivAlways), Pair(binding.includePushAlarm.tvReject, binding.includePushAlarm.ivReject))
                }
                R.id.constraint_main_push_reject ->
                {
                    selectedPushAlarmType = SettingEnum.PushAlarmType.REJECT
                    setSelectItemView(Pair(binding.includePushAlarm.tvReject, binding.includePushAlarm.ivReject))
                    setUnselectItemView(Pair(binding.includePushAlarm.tvArrive, binding.includePushAlarm.ivArrive), Pair(binding.includePushAlarm.tvAlways, binding.includePushAlarm.ivAlways))
                }
            }
        }

        binding.includePushAlarm.setOnConfirmClickListener {
            binding.slideMainSetting.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            vm.setPushAlarmType(selectedPushAlarmType)
        }
    }


    override fun setAfterBinding()
    {
        super.setAfterBinding()

        setNotDisturbTime()
        setPushAlarmListener()
        setLockPasswordListener()
    }

    private fun setDisableView()
    {
        binding.constraintMainNotDisturbTime.disabledClick()
        binding.constraintMainPushAlarm.disabledClick()
        binding.toggleBtn.disabledClick()
        binding.constraintMainLockApp.disabledClick()
    }

    fun setEnableView()
    {
        binding.constraintMainNotDisturbTime.enabledClick()
        binding.constraintMainPushAlarm.enabledClick()
        binding.toggleBtn.enabledClick()
        binding.constraintMainLockApp.enabledClick()
    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return

        parentView.getCurrentPage().observe(this) {
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

        vm.notDisturbTime.observe(this) {
            if(it == "")
            {
                binding.toggleBtn.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_toggle_off)
            }
            else
            {
                binding.toggleBtn.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_toggle_on)
            }
        }

        vm.navigator.observe(requireActivity()) { navigator ->
            SopoLog.d("navigator[$navigator]")

            when(navigator)
            {
                NavigatorConst.TO_BACK_SCREEN ->
                {
                    if(binding.slideMainSetting.panelState != SlidingUpPanelLayout.PanelState.COLLAPSED) return@observe

                    FragmentManager.refreshMove(parentView, TabCode.MY_MENU_MAIN.apply {
                        FRAGMENT = MenuFragment.newInstance()
                    }, MenuMainFragment.viewId)
                }
                NavigatorConst.TO_NOT_DISTURB ->
                {
                    parentView.hideTab()

                    binding.includeLockApp.root.makeGone()
                    binding.includePushAlarm.root.makeGone()
                    binding.includeNotDisturbTime.root.makeVisible()

                    initNotDisturbTime()

                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.slideMainSetting.panelState =
                            SlidingUpPanelLayout.PanelState.EXPANDED
                    }, 200)
                }
                NavigatorConst.TO_SET_NOTIFY_OPTION ->
                {
                    parentView.hideTab()

                    binding.includeNotDisturbTime.root.makeGone()
                    binding.includeLockApp.root.makeGone()
                    binding.includePushAlarm.root.makeVisible()

                    setUnselectItemView(Pair(binding.includePushAlarm.tvAlways, binding.includePushAlarm.ivAlways), Pair(binding.includePushAlarm.tvArrive, binding.includePushAlarm.ivArrive), Pair(binding.includePushAlarm.tvAlways, binding.includePushAlarm.ivAlways))

                    when(vm.pushAlarmType.value)
                    {
                        SettingEnum.PushAlarmType.ALWAYS -> setSelectItemView(Pair(binding.includePushAlarm.tvAlways, binding.includePushAlarm.ivAlways))
                        SettingEnum.PushAlarmType.ARRIVE -> setSelectItemView(Pair(binding.includePushAlarm.tvArrive, binding.includePushAlarm.ivArrive))
                        SettingEnum.PushAlarmType.REJECT -> setSelectItemView(Pair(binding.includePushAlarm.tvReject, binding.includePushAlarm.ivReject))
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.slideMainSetting.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                    }, 200)
                }
                NavigatorConst.TO_UPDATE_APP_PASSWORD ->
                {
                    parentView.hideTab()

                    binding.includeLockApp.root.makeVisible()
                    binding.includeNotDisturbTime.root.makeGone()
                    binding.includePushAlarm.root.makeGone()

                    setUnselectItemView(Pair(binding.includeLockApp.tvSetOn, binding.includeLockApp.ivSetOn), Pair(binding.includeLockApp.tvSetOff, binding.includeLockApp.ivSetOff))

                    if(vm.isSetOfSecurity.value ?: 0 > 0)
                    {
                        setSelectItemView(Pair(binding.includeLockApp.tvSetOn, binding.includeLockApp.ivSetOn))
                        binding.includeLockApp.tvLockPasswordChange.enabledClick()
                        binding.includeLockApp.tvLockPasswordChange.backgroundTintList = null
                        binding.includeLockApp.tvLockPasswordChange.convertTextColor(R.color.COLOR_MAIN_700)
                    }
                    else
                    {
                        setSelectItemView(Pair(binding.includeLockApp.tvSetOff, binding.includeLockApp.ivSetOff))
                        binding.includeLockApp.tvLockPasswordChange.disabledClick()
                        binding.includeLockApp.tvLockPasswordChange.backgroundTintList = resources.getColorStateList(R.color.COLOR_GRAY_200, null)
                        binding.includeLockApp.tvLockPasswordChange.convertTextColor(R.color.COLOR_GRAY_400)
                    }

                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        binding.slideMainSetting.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                    }, 200)
                }
            }
        }

        vm.showSetPassword.observe(requireActivity(), Observer {
            if(it)
            {
                vm.postNavigator(NavigatorConst.TO_NOT_DISTURB)
            }
            else
            {
            }
        })
    }

    companion object
    {
        fun newInstance(): SettingFragment
        {
            return SettingFragment()
        }
    }
}