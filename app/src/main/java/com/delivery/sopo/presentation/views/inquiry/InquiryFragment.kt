package com.delivery.sopo.presentation.views.inquiry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentInquiryBinding
import com.delivery.sopo.databinding.ItemInquiryTabBinding
import com.delivery.sopo.enums.InquiryStatusEnum
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.reduceSensitive
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.presentation.consts.IntentConst
import com.delivery.sopo.presentation.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.presentation.views.adapter.ViewPagerAdapter
import com.delivery.sopo.presentation.views.main.MainView
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.util.ui_util.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class InquiryFragment: BaseFragment<FragmentInquiryBinding, InquiryViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_inquiry
    override val vm: InquiryViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainInquiry }

    private val parentView: MainView by lazy { activity as MainView }

    lateinit var ongoingTabBinding: ItemInquiryTabBinding
    lateinit var completedTabBinding: ItemInquiryTabBinding

    var isRefresh = true
    var returnType = 0

    var inquiryStatus: InquiryStatusEnum = InquiryStatusEnum.ONGOING

    val broadcastReceiver: BroadcastReceiver = object: BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?)
        {
            intent ?: return

            SopoLog.d("Registered Action ${intent.action}")

            when(intent.action)
            {
                IntentConst.Action.REGISTERED_ONGOING_PARCEL ->
                {
                    binding.viewPagerInquiryType.currentItem = 0
                }
                IntentConst.Action.REGISTERED_COMPLETED_PARCEL ->
                {
                    val data = intent.getStringExtra("REGISTERED_DATE")
                    SopoLog.d("DATE => $data")
                    binding.viewPagerInquiryType.currentItem = 1
                }
                else ->
                {
                    SopoLog.d("NO ACTION")
                    return
                }
            }
        }
    }

    override fun onResume()
    {
        super.onResume()

        val filter = IntentFilter().apply {
            addAction(IntentConst.Action.REGISTERED_ONGOING_PARCEL)
            addAction(IntentConst.Action.REGISTERED_COMPLETED_PARCEL)
        }

        parentView.registerReceiver(broadcastReceiver, filter)
    }

    override fun onPause()
    {
        super.onPause()

        SopoLog.d("onPause")
        parentView.unregisterReceiver(broadcastReceiver)
    }

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        isRefresh = bundle.getBoolean("IS_REFRESH")
        returnType = bundle.getInt("RETURN_TYPE")
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        setViewPager(binding.viewPagerInquiryType)
        connectTabAndViewPager(binding.viewPagerInquiryType, binding.tabLayoutInquiryType)
        setOnTabSelectedListener()
        processReturnType()
    }

    override fun setObserve()
    {
        super.setObserve()

        vm.updatableParcelIds.observe(this) { parcelIds ->
            if(parcelIds.isEmpty()) return@observe // TODO 다중 택배 가져오기

            SopoLog.d("업데이트 가능한 택배 아이디 ${parcelIds.joinToString()}")

            vm.onUpdateParcels(parcelIds)

            val snackBar =
                CustomSnackBar.make(view = binding.tabLayoutInquiryType, content = "방금 ${parcelIds.size}개의 배송정보가 업데이트 되었습니다.", data = Unit, type = SnackBarEnum.COMMON)
            snackBar.show()
        }

        vm.cntOfBeDelivered.observe(this) { cnt ->
            SopoLog.d("테스트 도착 상태 갯수 $cnt")
            completedTabBinding.updateCount = cnt
        }

        vm.isConfirmDelete.observe(this) { isConfirmDelete ->

            if(!isConfirmDelete)
            {
                vm.recoverDeleteParcels()
                return@observe
            }

            Handler(Looper.myLooper()!!).postDelayed(Runnable {
                vm.confirmDeleteParcels()
            }, 5500)
        }

        vm.navigator.observe(this) { nav ->

            when(nav)
            {
                NavigatorConst.TO_DELETE ->
                {
                    TabCode.DELETE_PARCEL.FRAGMENT = DeleteParcelFragment.newInstance(inquiryStatus)
                    FragmentManager.move(requireActivity(), TabCode.DELETE_PARCEL, InquiryMainFragment.viewId)
                }
            }

        }
    }

    private fun setViewPager(viewPager: ViewPager2)
    {
        val adapter =
            ViewPagerAdapter(requireActivity(), arrayListOf(OngoingTypeFragment(), CompletedTypeFragment()))

        viewPager.apply {
            this.adapter = adapter
            setPageTransformer(ZoomOutPageTransformer())
            offscreenPageLimit = 2
        }

        viewPager.reduceSensitive()
    }

    private fun connectTabAndViewPager(viewPager: ViewPager2, tabLayout: TabLayout)
    {
        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->

            val tabBinding =
                DataBindingUtil.bind<ItemInquiryTabBinding>(LayoutInflater.from(requireContext())
                                                                .inflate(R.layout.item_inquiry_tab, null))
                    ?: throw NullPointerException("탭 오류")

            when(pos)
            {
                0 ->
                {
                    ongoingTabBinding = tabBinding
                    tabBinding.tvInquiryTabName.text = "배송중"
                }
                1 ->
                {
                    completedTabBinding = tabBinding
                    tabBinding.tvInquiryTabName.text = "배송완료"
                }
            }

            tab.customView = tabBinding.root
        }.attach()
    }

    private fun setOnTabSelectedListener()
    {

        val onTabSelectedListener = object: TabLayout.OnTabSelectedListener
        {
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                val pos = tab?.position ?: 0

                when(pos)
                {
                    ONGOING_TYPE ->
                    {
                        inquiryStatus = InquiryStatusEnum.ONGOING
                        ongoingTabBinding.tvInquiryTabName.typeface =
                            ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
                    }
                    COMPETED_TYPE ->
                    {
                        inquiryStatus = InquiryStatusEnum.COMPLETE
                        completedTabBinding.tvInquiryTabName.typeface =
                            ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)

                        vm.clearDeliveredBadge()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?)
            {
                val pos = tab?.position ?: 0

                when(pos)
                {
                    ONGOING_TYPE ->
                    {
                        ongoingTabBinding.tvInquiryTabName.typeface =
                            ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)
                    }
                    COMPETED_TYPE ->
                    {
                        completedTabBinding.tvInquiryTabName.typeface =
                            ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?)
            {
            }
        }

        binding.tabLayoutInquiryType.addOnTabSelectedListener(onTabSelectedListener)
    }

    fun processReturnType()
    {
        when(returnType)
        {
            1 ->
            {
                parentView.showTab()
            }
            2 ->
            {
                parentView.showTab()

                CoroutineScope(Dispatchers.Main).launch {

                    val parcelStatuses = vm.getDeletableParcelStatuses().apply {
                        if(isEmpty()) return@launch
                    }

                    vm.startDeleteCount()

                    val snackBar = CustomSnackBar.make<Unit>(view = mainLayout, content = "${parcelStatuses.size}개 항목이 삭제되었습니다.", data = Unit, SnackBarEnum.CONFIRM_DELETE, Pair("실행취소") { vm.stopDeleteCount() })
                    snackBar.setDuration(5000)
                    snackBar.show()
                }
            }

        }
    }

    companion object
    {
        /**
         * returnType 0 base
         *            1 Inquiry Tab에서 다른 페이지 -> 메인 페이지, Tab 상태를 변경
         *            2 Inquiry Tab에서 삭제 페이지 -> 메인 페이지, Tab 상태를 변경 & 삭제 확인 Snack Bar 호출
         */

        const val ONGOING_TYPE: Int = 0
        const val COMPETED_TYPE: Int = 1

        fun newInstance(isRefresh: Boolean = true, returnType: Int): InquiryFragment
        {
            val args = Bundle().apply {
                putBoolean("IS_REFRESH", isRefresh)
                putInt("RETURN_TYPE", returnType)
            }

            return InquiryFragment().apply {
                arguments = args
            }
        }
    }

}