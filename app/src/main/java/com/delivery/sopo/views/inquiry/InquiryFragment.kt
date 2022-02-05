package com.delivery.sopo.views.inquiry

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.View.*
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentInquiryReBinding
import com.delivery.sopo.databinding.ItemInquiryTabBinding
import com.delivery.sopo.databinding.PopupMenuViewBinding
import com.delivery.sopo.enums.*
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.inquiry.InquiryMenuItem
import com.delivery.sopo.models.mapper.MenuMapper
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.views.adapter.PopupMenuListAdapter
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.system.exitProcess

class InquiryFragment: BaseFragment<FragmentInquiryReBinding, InquiryViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_inquiry_re
    override val vm: InquiryViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainInquiry }

    private val parentView: MainView by lazy { activity as MainView }

    lateinit var firstBinding: ItemInquiryTabBinding
    lateinit var secondBinding: ItemInquiryTabBinding

    private var menuPopUpWindow: PopupWindow? = null

    var isRefresh = true
    var returnType = 0

    var inquiryStatus: InquiryStatusEnum = InquiryStatusEnum.ONGOING

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        onSOPOBackPressedListener = object: OnSOPOBackPressListener
        {
            override fun onBackPressedInTime()
            {
                Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다. 메인", 2000)
                    .apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }
                    .show()
            }

            override fun onBackPressedOutTime()
            {
                ActivityCompat.finishAffinity(parentView)
                exitProcess(0)
            }
        }
    }

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        isRefresh = bundle.getBoolean("IS_REFRESH")
        returnType = bundle.getInt("RETURN_TYPE")
    }

    override fun setBeforeBinding() { super.setBeforeBinding() }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        setViewPager(binding.viewPagerInquiryType)
        connectTabAndViewPager(binding.viewPagerInquiryType, binding.tabLayoutInquiryType)

        binding.tabLayoutInquiryType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                val pos = tab?.position?:0

                when(pos)
                {
                    0 ->
                    {
                        if(!::firstBinding.isInitialized) return
                        inquiryStatus = InquiryStatusEnum.ONGOING
                        firstBinding.tvInquiryTabName.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
                    }
                    1->
                    {
                        if(!::secondBinding.isInitialized) return
                        inquiryStatus = InquiryStatusEnum.COMPLETE


                        vm.clearDeliveredBadge()

                        secondBinding.tvInquiryTabName.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?)
            {
                val pos = tab?.position?:0

                when(pos)
                {
                    0 ->
                    {
                        if(!::firstBinding.isInitialized) return
                        firstBinding.tvInquiryTabName.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)
                    }
                    1->
                    {
                        if(!::secondBinding.isInitialized) return
                        secondBinding.tvInquiryTabName.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?)
            {
            }

        })

        returnType.apply {
            when(this)
            {
                0 ->
                {

                }
                1 ->
                {
                    parentView.showTab()
                }
                2 ->
                {
                    SopoLog.d("Delete Test 2")
                    parentView.showTab()
                    CoroutineScope(Dispatchers.Main).launch {

                        val parcelStatuses = vm.getDeletableParcelStatuses()

                        if(parcelStatuses.isEmpty()) return@launch

                        CustomSnackBar.make(mainLayout, "${parcelStatuses.size}개 항목이 삭제되었습니다.", 5000, SnackBarEnum.CONFIRM_DELETE, Pair("실행취소", {
                            vm.cancelToDelete(parcelStatuses)
                        })).show()

                        delay(5000)

                        Handler(Looper.myLooper()!!).postDelayed(Runnable { vm.onDeleteParcels() }, 5000)

                    }
                }
            }
        }

        binding.includeHeader.onRightClickListener = View.OnClickListener {
            openInquiryMenu(it)
        }
    }

    override fun onResume()
    {
        super.onResume()
        parentView.onBackPressedDispatcher.addCallback(parentView, onBackPressedCallback)
    }

    override fun setObserve()
    {
        super.setObserve()

        vm.cntOfBeDelivered.observe(this){ cnt ->
            secondBinding.updateCount = cnt
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

        val popUpView: PopupMenuViewBinding =
            PopupMenuViewBinding.inflate(LayoutInflater.from(requireContext())).also { v ->
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

    private fun setViewPager(viewPager: ViewPager2){
        val adapter = ViewPagerAdapter(requireActivity(), arrayListOf(OngoingTypeFragment(), CompletedTypeFragment()))

        viewPager.apply {
            this.adapter = adapter
            offscreenPageLimit = 2
        }
    }

    private fun connectTabAndViewPager(viewPager: ViewPager2, tabLayout: TabLayout){
        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->

            val tabBinding = DataBindingUtil.bind<ItemInquiryTabBinding>(LayoutInflater.from(requireContext()).inflate(R.layout.item_inquiry_tab , null)) ?:throw NullPointerException("탭 오류")

            when(pos)
            {
                0 ->
                {
                    firstBinding = tabBinding
                    tabBinding.tvInquiryTabName.text = "배송중"
                }
                1 ->
                {
                    secondBinding = tabBinding
                    tabBinding.tvInquiryTabName.text = "배송완료"
                    tabBinding.updateCount = 10
                }
            }

            tab.customView = tabBinding.root
        }.attach()
    }

    companion object
    {
        /**
         * returnType 0 base
         *            1 Inquiry Tab에서 다른 페이지 -> 메인 페이지, Tab 상태를 변경
         *            2 Inquiry Tab에서 삭제 페이지 -> 메인 페이지, Tab 상태를 변경 & 삭제 확인 Snack Bar 호출
         */

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