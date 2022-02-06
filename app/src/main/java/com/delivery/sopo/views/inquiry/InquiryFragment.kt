package com.delivery.sopo.views.inquiry

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.View.*
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentInquiryBinding
import com.delivery.sopo.databinding.ItemInquiryTabBinding
import com.delivery.sopo.databinding.PopupMenuViewBinding
import com.delivery.sopo.enums.*
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.inquiry.InquiryMenuItem
import com.delivery.sopo.models.mapper.MenuMapper
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.views.adapter.PopupMenuListAdapter
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.main.MainView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class InquiryFragment: BaseFragment<FragmentInquiryBinding, InquiryViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_inquiry
    override val vm: InquiryViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainInquiry }

    private val parentView: MainView by lazy { activity as MainView }

    lateinit var ongoingTabBinding: ItemInquiryTabBinding
    lateinit var completedTabBinding: ItemInquiryTabBinding

    private var menuPopUpWindow: PopupWindow? = null

    var isRefresh = true
    var returnType = 0

    var inquiryStatus: InquiryStatusEnum = InquiryStatusEnum.ONGOING

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


        binding.includeHeader.onRightClickListener = View.OnClickListener {
            openInquiryMenu(it)
        }
    }

    override fun setObserve()
    {
        super.setObserve()

        vm.cntOfBeDelivered.observe(this) { cnt ->
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
    }

    private fun openInquiryMenu(anchorView: View)
    {
        if(menuPopUpWindow != null)
        {
            menuPopUpWindow?.showAsDropDown(anchorView)
            return
        }

        val menu = PopupMenu(requireActivity(), anchorView).menu

        requireActivity().menuInflater.inflate(R.menu.inquiry_popup_menu, menu)

        val popUpView: PopupMenuViewBinding = PopupMenuViewBinding.inflate(LayoutInflater.from(requireContext())).also { v ->
                val popupMenuListAdapter =
                    PopupMenuListAdapter(MenuMapper.menuToMenuItemList(menu) as MutableList<InquiryMenuItem>)

                v.recyclerviewInquiryPopupMenu.also {
                    it.adapter = popupMenuListAdapter
                    val dividerItemDecoration =
                        DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.line_divider)!!)
                    it.addItemDecoration(dividerItemDecoration)

                    // 'Inquiry' 화면 우측 상단의 메뉴 아이템 이벤트
                    popupMenuListAdapter.setPopUpMenuOnclick(object: PopupMenuListAdapter.InquiryPopUpMenuItemOnclick
                                                             {
                                                                 override fun removeItem(v: View)
                                                                 { //삭제하기
                                                                     //                                                                     vm.onOpenDeleteView()
                                                                     TabCode.DELETE_PARCEL.FRAGMENT =
                                                                         DeleteParcelFragment.newInstance(inquiryStatus
                                                                                                              ?: InquiryStatusEnum.ONGOING)
                                                                     FragmentManager.move(requireActivity(), TabCode.DELETE_PARCEL, InquiryMainFragment.viewId)
                                                                     menuPopUpWindow?.dismiss()
                                                                 }

                                                                 override fun refreshItems(v: View)
                                                                 { // 새로고침
                                                                     //                                                                     vm.syncParcelsByOngoing()
                                                                     menuPopUpWindow?.dismiss()
                                                                 }

                                                                 override fun help(v: View)
                                                                 { // 도움말
                                                                     menuPopUpWindow?.dismiss()
                                                                 }
                                                             })
                }
            }

        menuPopUpWindow =
            PopupWindow(popUpView.root, SizeUtil.changeDpToPx(binding.root.context, 175F), ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                showAsDropDown(anchorView)
            }
    }

    private fun setViewPager(viewPager: ViewPager2)
    {
        val adapter =
            ViewPagerAdapter(requireActivity(), arrayListOf(OngoingTypeFragment(), CompletedTypeFragment()))

        viewPager.apply {
            this.adapter = adapter
            offscreenPageLimit = 2
        }
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

                    CustomSnackBar.make(mainLayout, "${parcelStatuses.size}개 항목이 삭제되었습니다.", 5000, SnackBarEnum.CONFIRM_DELETE, Pair("실행취소", {
                        vm.stopDeleteCount()
                    })).show()
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