package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.delivery.sopo.R
import com.delivery.sopo.data.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.databinding.FragmentDeleteParcelBinding
import com.delivery.sopo.databinding.PopupMenuViewBinding
import com.delivery.sopo.enums.*
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.interfaces.listener.ParcelEventListener
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.inquiry.InquiryMenuItem
import com.delivery.sopo.models.mapper.MenuMapper
import com.delivery.sopo.util.*
import com.delivery.sopo.viewmodels.inquiry.DeleteParcelViewModel
import com.delivery.sopo.views.adapter.InquiryListAdapter
import com.delivery.sopo.views.adapter.PopupMenuListAdapter
import com.delivery.sopo.views.main.MainView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class DeleteParcelFragment: BaseFragment<FragmentDeleteParcelBinding, DeleteParcelViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_delete_parcel
    override val vm: DeleteParcelViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainInquiry }

    private val parentView: MainView by lazy { activity as MainView }

    // 곧 도착 택배 리스트 adapter, 등록된 택배(진행 중) 리스트 adapter, 도착 완료된 택배 리스트 adapter
    private lateinit var soonArrivalParcelAdapter: InquiryListAdapter
    private lateinit var registeredParcelAdapter: InquiryListAdapter
    private lateinit var completedParcelAdapter: InquiryListAdapter

    private lateinit var historyPopUpWindow: PopupWindow

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        if(bundle.getString("INQUIRY_TYPE") ?: "ONGOING" == "ONGOING") vm.setInquiryStatus(InquiryStatusEnum.ONGOING)
        else vm.setInquiryStatus(InquiryStatusEnum.COMPLETE)
    }

    override fun setBeforeBinding()
    {
        parentView.hideTab()

        useCommonBackPressListener(isUseCommon = true)

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent(isUseCommon = true)
        {
            override fun onBackPressed()
            {
                super.onBackPressed()

                TabCode.INQUIRY.FRAGMENT = InquiryFragment.newInstance(returnType = 1)
                FragmentManager.move(parentView, TabCode.INQUIRY, InquiryMainFragment.viewId)
            }
        }
    }

    override fun setAfterBinding()
    {
        setAdapters()
        setListener()

        binding.vInnerCompletedSpace.setOnTouchListener { v, event ->
            return@setOnTouchListener binding.linearMainMonthSelector.dispatchTouchEvent(event)
        }

        binding.nestSvMainCompleted.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if(scrollY > 0 && oldScrollY == 0) vm.isMonthClickable = false
            else if(scrollY == 0 && oldScrollY > 0) vm.isMonthClickable = true
        }

        updateCompletedDateSector()
    }

    private fun getAdapter(cntOfSelectedForDelete: MutableLiveData<Int>, inquiryItemTypeEnum: InquiryItemTypeEnum): InquiryListAdapter
    {
        return InquiryListAdapter(cntOfSelectedItemForDelete = cntOfSelectedForDelete, parcelType = inquiryItemTypeEnum).apply {
            this.setOnParcelClickListener(getParcelClicked())
            changeParcelDeleteMode(true)
        }
    }

    private fun setAdapters()
    {
        getAdapter(vm.cntOfSelectedItemForDelete, InquiryItemTypeEnum.Soon).let { adapter ->
            soonArrivalParcelAdapter = adapter
            binding.recyclerviewSoonArrival.adapter = soonArrivalParcelAdapter
            val animator = binding.recyclerviewSoonArrival.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }

        getAdapter(vm.cntOfSelectedItemForDelete, InquiryItemTypeEnum.Registered).let { adapter ->
            registeredParcelAdapter = adapter
            binding.recyclerviewRegisteredParcel.adapter = registeredParcelAdapter
            val animator = binding.recyclerviewSoonArrival.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }
        getAdapter(vm.cntOfSelectedItemForDelete, InquiryItemTypeEnum.Complete).let { adapter ->
            completedParcelAdapter = adapter
            binding.recyclerviewCompleteParcel.adapter = completedParcelAdapter
            val animator = binding.recyclerviewSoonArrival.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }
    }

    override fun setObserve()
    {
        super.setObserve()

//        if(activity == null) return
//
//        parentView.currentPage.observe(parentView) {
//            if(it != null && it == TabCode.secondTab)
//            {
//                parentView.onBackPressedDispatcher.addCallback(parentView, onBackPressedCallback)
//            }
//        }

        vm.inquiryStatus.observe(parentView) {
            if(it == InquiryStatusEnum.COMPLETE) vm.getCompleteParcelMonth()
        }

        vm.navigator.observe(requireActivity()) { navigator ->

            when(navigator)
            {
                NavigatorEnum.INQUIRY_PARCEL ->
                {
                    TabCode.INQUIRY.FRAGMENT = InquiryFragment.newInstance(returnType = 1)
                    FragmentManager.move(parentView, TabCode.INQUIRY, InquiryMainFragment.viewId)
                }
                NavigatorEnum.DELETE_PARCEL ->
                {
                    val deleteParcelIds =
                        soonArrivalParcelAdapter.getSelectedListData() + registeredParcelAdapter.getSelectedListData() + completedParcelAdapter.getSelectedListData()

                    CoroutineScope(Dispatchers.Main).launch {
                        vm.updateParcelToDeleteParcels(deleteParcelIds)

                        TabCode.INQUIRY.FRAGMENT = InquiryFragment.newInstance(returnType = 2)
                        FragmentManager.move(parentView, TabCode.INQUIRY, InquiryMainFragment.viewId)
                    }
                }
            }


        }

        vm.isSelectAllItems.observe(parentView) { isSelect ->
            if(vm.getCurrentScreenStatus() == InquiryStatusEnum.ONGOING)
            {
                soonArrivalParcelAdapter.setSelectAll(isSelect)
                registeredParcelAdapter.setSelectAll(isSelect)
            }
            else
            {
                completedParcelAdapter.setSelectAll(isSelect)
            }
        }

        var isShowDeleteSnackBar = false

        vm.cntOfSelectedItemForDelete.observe(parentView) { cnt ->

            SopoLog.d("삭제 예정 카운트 $cnt")

            if(cnt == 0)
            {
                isShowDeleteSnackBar = false
                AnimationUtil.slideDown(binding.constraintSnackBarDelete)
            }
            else if(cnt == 1 && !isShowDeleteSnackBar)
            {
                isShowDeleteSnackBar = true
                AnimationUtil.slideUp(binding.constraintSnackBarDelete)
            }
        }

        // 배송중 , 등록된 택배 리스트
        vm.ongoingList.observe(requireActivity(), Observer { list ->

            SopoLog.d("진행중인 택배 갯수 [size:${list.size}]")

            soonArrivalParcelAdapter.separateDeliveryListByStatus(list)
            registeredParcelAdapter.separateDeliveryListByStatus(list)

            viewSettingForSoonArrivalList(soonArrivalParcelAdapter.getListSize())
            viewSettingForRegisteredList(registeredParcelAdapter.getListSize())
        })

        // 배송완료 리스트.
        vm.completeList.observe(requireActivity(), Observer { list ->
            completedParcelAdapter.notifyChanged(list)
        })

        // 배송완료 화면에서 표출 가능한 년월 리스트
        vm.histories.observe(requireActivity()) { dates ->

            if(dates.isEmpty()) return@observe

            CoroutineScope(Dispatchers.Main).launch {

                val latestDate = try
                {
                    dates.first { date -> date.count > 0 }
                }
                catch(e: NoSuchElementException)
                {
                    dates.first()
                }

                vm.updateCompletedParcelCalendar(latestDate.year)

                // TODO 다른 곳으로 빼야할듯
                binding.constraintInnerYearSpinner.setOnClickListener { v ->
                    drawCompletedParcelHistoryPopMenu(v, dates)
                }
            }

        }

        vm.monthsOfCalendar.observe(requireActivity()) { list ->

            setDefaultMonthSelector()
            vm.selectedDate.postValue("")

            vm.setIsSelectAllItems(false)

            val reversedList = list.reversed()

            CoroutineScope(Dispatchers.Main).launch {

                reversedList.forEach {

                    if(it.item.count > 0 && it.isSelect)
                    {
                        vm.selectedDate.postValue("${it.item.year}년 ${it.item.month}월")
                    }

                    val (clickable, textColor, font) = if(it.item.count > 0)
                    {
                        Triple(first = true, second = if(it.isSelect) ContextCompat.getColor(requireContext(), R.color.MAIN_WHITE)
                        else ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800), third = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold))
                    }
                    else
                    {
                        Triple(first = false, second = ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_300), third = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium))
                    }

                    when(it.item.month)
                    {
                        "01" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvJan, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "02" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvFeb, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "03" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvMar, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "04" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvApr, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "05" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvMay, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "06" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvJun, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "07" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvJul, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "08" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvAug, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "09" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvSep, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "10" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvOct, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "11" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvNov, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                        "12" ->
                        {
                            val background =
                                if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale) else null
                            setMonthIconUI(tv = binding.tvDec, font = font, textColor = textColor, clickable = clickable, background = background)
                        }
                    }

                }
            }


        }

        vm.selectedDate.observe(requireActivity()) { date ->
            if(date == "") return@observe
            val searchDate = date.replace("년 ", "").replace("월", "")
            vm.refreshCompleteParcelsByDate(searchDate)
        }
    }

    private fun getParcelClicked(): ParcelEventListener
    {
        return object: ParcelEventListener()
        {
            override fun onEnterParcelDetailClicked(view: View, type: InquiryStatusEnum, parcelId: Int)
            {
                super.onEnterParcelDetailClicked(view, type, parcelId)

                TabCode.INQUIRY_DETAIL.FRAGMENT = ParcelDetailView.newInstance(parcelId)
                FragmentManager.add(requireActivity(), TabCode.INQUIRY_DETAIL, InquiryMainFragment.viewId)
            }
        }
    }

    // 배송완료 화면에서 년/월을 눌렀을 시 팝업 메뉴가 나온다.
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun drawCompletedParcelHistoryPopMenu(anchorView: View, histories: List<CompletedParcelHistory>)
    {
        val historyPopUpView: PopupMenuViewBinding =
            PopupMenuViewBinding.inflate(LayoutInflater.from(context)).also { v ->

                val inquiryMenuItems =
                    MenuMapper.completeParcelStatusDTOToMenuItem(histories) as MutableList<InquiryMenuItem>

                val popupMenuListAdapter = PopupMenuListAdapter(inquiryMenuItems)

                v.recyclerviewInquiryPopupMenu.also {
                    it.adapter = popupMenuListAdapter
                    val dividerItemDecoration =
                        DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.line_divider)!!)
                    it.addItemDecoration(dividerItemDecoration)

                    val historyPopUpItemOnClick =
                        object: PopupMenuListAdapter.HistoryPopUpItemOnclick
                        {
                            override fun changeTimeCount(v: View, year: String)
                            {
                                vm.changeCompletedParcelHistoryDate(year = year)
                                historyPopUpWindow.dismiss()
                            }
                        }

                    popupMenuListAdapter.setHistoryPopUpItemOnclick(historyPopUpItemOnClick)

                    it.scrollBarFadeDuration = 800
                }
            }

        CoroutineScope(Dispatchers.Main).launch {

            historyPopUpWindow = if(histories.size > 2)
            {
                PopupWindow(historyPopUpView.root, SizeUtil.changeDpToPx(binding.root.context, 160F), SizeUtil.changeDpToPx(binding.root.context, 35 * 6F), true).apply {

                    showAsDropDown(anchorView, -80, 0, Gravity.CENTER)
                }
            }
            else
            {
                PopupWindow(historyPopUpView.root, SizeUtil.changeDpToPx(binding.root.context, 160F), ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {

                    showAsDropDown(anchorView, -80, 0, Gravity.CENTER)

                }
            }
        }
    }

    private fun setListener()
    { // 당겨서 새로고침 !
        // 배송완료 리스트의 마지막 행까지 내려갔다면 다음 데이터를 요청한다(페이징)

        val onScrollListener = object: RecyclerView.OnScrollListener()
        {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
            {
                super.onScrollStateChanged(recyclerView, newState)

                if(!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) // 리스트뷰의 마지막
                {
                    val inquiryDate =
                        vm.selectedDate.value?.replace("년 ", "")?.replace("월", "") ?: return

                    SopoLog.d("완료 택배 RecyclerView $inquiryDate")

                    CoroutineScope(Dispatchers.IO).launch {
                        vm.getCompleteParcelsWithPaging(inquiryDate = inquiryDate)

                    }
                }
            }
        }

        binding.recyclerviewCompleteParcel.addOnScrollListener(onScrollListener)
    }

    // '곧 도착' 리스트의 아이템의 개수에 따른 화면세팅
    private fun viewSettingForSoonArrivalList(listSize: Int)
    {
        if(listSize > 0)
        {
            binding.constraintSoonArrival.visibility = VISIBLE
            return
        }

        binding.constraintSoonArrival.visibility = GONE

        when(listSize)
        {

            /*// 아이템의 개수가 0개일때
            0 ->
            {
                // '곧 도착' 텍스트부터 리스트뷰까지 잡혀있는 부모뷰를 GONE 처리
                binding.constraintSoonArrival.visibility = GONE
                // '더보기'를  선택할 수 있는 부모뷰 GONE 처리
                binding.linearMoreViewParent.visibility = GONE
                // '곧 도착'과 '등록된 택배'의 사이에 적절한 공백을 담당하는 뷰 GONE 처리
                binding.vMoreView.visibility = GONE
            }
            1 ->
            {
                binding.constraintSoonArrival.visibility = VISIBLE
                binding.linearMoreViewParent.visibility = GONE
                binding.vMoreView.visibility = INVISIBLE
            }
            2 ->
            {
                binding.constraintSoonArrival.visibility = VISIBLE
                binding.linearMoreViewParent.visibility = GONE
                binding.vMoreView.visibility = INVISIBLE
            }
            else ->
            {
                binding.constraintSoonArrival.visibility = VISIBLE
                binding.linearMoreViewParent.visibility = VISIBLE
                binding.vMoreView.visibility = GONE
            }*/
        }
    }

    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    private fun viewSettingForRegisteredList(listSize: Int)
    {
        when(listSize)
        {
            0 ->
            {
                binding.constraintRegisteredArrival.visibility = GONE
            }
            else ->
            {
                binding.constraintRegisteredArrival.visibility = VISIBLE
            }
        }
    }

    private fun setDefaultMonthSelector()
    {
        val (clickable, textColor, font) = Triple(first = false, second = ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_300), third = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium))

        setMonthIconUI(tv = binding.tvJan, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvFeb, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvMar, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvApr, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvMay, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvJun, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvJul, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvAug, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvSep, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvOct, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvNov, font = font, textColor = textColor, clickable = clickable)
        setMonthIconUI(tv = binding.tvDec, font = font, textColor = textColor, clickable = clickable)
    }

    private fun setMonthIconUI(tv: TextView, font: Typeface?, textColor: Int, clickable: Boolean, background: Drawable? = null)
    {
        tv.apply {
            setTextColor(textColor)
            typeface = font
            isClickable = clickable
            isFocusable = clickable
            this.background = background
        }
    }

    private fun updateCompletedDateSector()
    {
        SopoLog.i("호출")

        val onGlobalLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener
        {
            override fun onGlobalLayout()
            {
                // 'year spinner'높이 수치만큼 'month sector'의 상단 공백을 생성
                val yearSpinnerHeight: Int = binding.linearMainYearSpinner.height

                (binding.linearMainMonthSelector.layoutParams as FrameLayout.LayoutParams).apply {
                    topMargin = yearSpinnerHeight
                }

                // 'year spinner'높이 수치만큼 'completed space'의 상단 공백을 생성
                // 'month sector'높이 수치만큼 'completed space'의 높이변경
                val monthSelectorHeight = binding.linearMainMonthSelector.height

                (binding.vInnerCompletedSpace.layoutParams as LinearLayout.LayoutParams).apply {
                    this.topMargin = yearSpinnerHeight
                    this.height = monthSelectorHeight
                }

                // 뷰 조절 후 옵저빙 리스너 제거
                binding.frameMainCompleteInquiry.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }

        binding.frameMainCompleteInquiry.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    companion object
    {
        fun newInstance(statusType: InquiryStatusEnum): DeleteParcelFragment
        {
            val args = Bundle().apply {
                putString("INQUIRY_TYPE", statusType.name)
            }

            return DeleteParcelFragment().apply {
                arguments = args
            }
        }
    }
}