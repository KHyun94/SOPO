package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.databinding.FragmentInquiryReBinding
import com.delivery.sopo.databinding.PopupMenuViewBinding
import com.delivery.sopo.enums.InquiryItemTypeEnum
import com.delivery.sopo.enums.InquiryStatusEnum
import com.delivery.sopo.enums.OptionalTypeEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.interfaces.listener.ParcelEventListener
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.inquiry.InquiryMenuItem
import com.delivery.sopo.models.mapper.MenuMapper
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.views.adapter.InquiryListAdapter
import com.delivery.sopo.views.adapter.PopupMenuListAdapter
import com.delivery.sopo.views.dialog.OptionalClickListener
import com.delivery.sopo.views.dialog.OptionalDialog
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.function.Function
import kotlin.system.exitProcess

class InquiryFragment: BaseFragment<FragmentInquiryReBinding, InquiryViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_inquiry_re
    override val vm: InquiryViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainInquiry }

    private val parentView: MainView by lazy { activity as MainView }

    // 곧 도착 택배 리스트 adapter, 등록된 택배(진행 중) 리스트 adapter, 도착 완료된 택배 리스트 adapter
    private lateinit var soonArrivalParcelAdapter: InquiryListAdapter
    private lateinit var registeredParcelAdapter: InquiryListAdapter
    private lateinit var completedParcelAdapter: InquiryListAdapter

    private var menuPopUpWindow: PopupWindow? = null
    private var historyPopUpWindow: PopupWindow? = null

    private var refreshDelay: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        onSOPOBackPressedListener = object: OnSOPOBackPressListener
        {
            override fun onBackPressedInTime()
            {
                Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000).apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }.show()
            }

            override fun onBackPressedOutTime()
            {
                ActivityCompat.finishAffinity(parentView)
                exitProcess(0)
            }
        }
    }

    override fun setBeforeBinding()
    {

    }

    override fun setAfterBinding()
    {
        setAdapters()
        setListener()

        binding.includeHeader.onRightClickListener = View.OnClickListener {
            openInquiryMenu(it)
        }

        binding.vEmpty2.setOnTouchListener { v, event ->
            return@setOnTouchListener binding.linearMonthSelector.dispatchTouchEvent(event)
        }

        binding.nestScrollViewComplete.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if(scrollY > 0 && oldScrollY == 0) vm.isMonthClickable = false
            else if(scrollY == 0 && oldScrollY > 0) vm.isMonthClickable = true
        }

        updateCompleteUI()
    }

    private fun getAdapter(inquiryItemTypeEnum: InquiryItemTypeEnum): InquiryListAdapter
    {
        return InquiryListAdapter(parcelType = inquiryItemTypeEnum).apply {
            this.setOnParcelClickListener(getParcelClicked())
        }
    }

    private fun setAdapters()
    {
        getAdapter(InquiryItemTypeEnum.Soon).let { adapter ->
            soonArrivalParcelAdapter = adapter
            binding.recyclerviewSoonArrival.adapter = soonArrivalParcelAdapter
            soonArrivalParcelAdapter.isFullListItem(true)
            val animator = binding.recyclerviewSoonArrival.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }
        getAdapter(InquiryItemTypeEnum.Registered).let { adapter ->
            registeredParcelAdapter = adapter
            binding.recyclerviewRegisteredParcel.adapter = registeredParcelAdapter
            val animator = binding.recyclerviewSoonArrival.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }
        getAdapter(InquiryItemTypeEnum.Complete).let { adapter ->
            completedParcelAdapter = adapter
            binding.recyclerviewCompleteParcel.adapter = completedParcelAdapter
            val animator = binding.recyclerviewSoonArrival.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }
    }

    private fun activateInquiryStatus(tv: TextView)
    {
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.MAIN_WHITE))
        tv.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.border_all_rounded_light_black)
        tv.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
    }

    private fun inactivateInquiryStatus(tv: TextView)
    {
        tv.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
        tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.border_all_rounded_color_gray_400)
    }

    override fun setObserve()
    {
        super.setObserve()

        if(activity == null) return
        parentView.currentPage.observe(requireActivity()) {
            if(it != null && it == TabCode.secondTab)
            {
                requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)
            }
        }

        vm.inquiryStatus.observe(requireActivity()) {

            when(it)
            {
                InquiryStatusEnum.ONGOING ->
                { // '배송 중' 화면
                    activateInquiryStatus(binding.tvOngoing)
                    inactivateInquiryStatus(binding.tvComplete)
                }

                InquiryStatusEnum.COMPLETE ->
                { // '배송 완료' 화면
                    activateInquiryStatus(binding.tvComplete)
                    inactivateInquiryStatus(binding.tvOngoing)
                }
            }
        }

        // 배송중 , 등록된 택배 리스트
        vm.ongoingList.observe(requireActivity(), Observer { list ->

            SopoLog.d("진행중인 택배 갯수 [size:${list.size}]")

            if(list.size == 0) binding.linearNoItem.visibility = VISIBLE
            else binding.linearNoItem.visibility = GONE

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

            CoroutineScope(Dispatchers.Main).launch {
                if(dates.isEmpty())
                {
                    SopoLog.d("완료 택배가 없습니다.")

                    binding.includeCompleteNoItem.visible = View.GONE

                    return@launch
                }

                SopoLog.d("완료 택배가 있습니다. ${dates.size}")

                binding.includeCompleteNoItem.visible = View.GONE

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
                binding.constraintYearSpinner.setOnClickListener { v ->
                    drawCompletedParcelHistoryPopMenu(v, dates)
                }
            }

        }

        vm.monthsOfCalendar.observe(requireActivity()) { list ->

            setDefaultMonthSelector()
            vm.selectedDate.postValue("")

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
                            binding.tvJan.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "02" ->
                        {
                            binding.tvFeb.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "03" ->
                        {
                            binding.tvMar.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "04" ->
                        {
                            binding.tvApr.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "05" ->
                        {
                            binding.tvMay.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "06" ->
                        {
                            binding.tvJun.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "07" ->
                        {
                            binding.tvJul.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "08" ->
                        {
                            binding.tvAug.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "09" ->
                        {
                            binding.tvSep.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "10" ->
                        {
                            binding.tvOct.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "11" ->
                        {
                            binding.tvNov.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
                        }
                        "12" ->
                        {
                            binding.tvDec.apply {
                                setTextColor(textColor)
                                typeface = font
                                isClickable = clickable
                                isFocusable = clickable
                                background =
                                    if(it.isSelect) ContextCompat.getDrawable(requireContext(), R.drawable.oval_24dp_gray_scale)
                                    else null
                            }
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
            override fun onMaintainParcelClicked(view: View, type: InquiryStatusEnum, parcelId: Int)
            {
                super.onMaintainParcelClicked(view, type, parcelId)

                OptionalDialog(
                    optionalType = OptionalTypeEnum.LEFT,
                    titleIcon = 0,
                    title = "이 아이템을 제거할까요?",
                    subTitle = "고객의 정보가 삭제되며 복구가 불가능합니다.",
                    content = """
                    배송 상태가 2주간 확인되지 않고 있어요.
                    등록된 송장번호가 유효하지 않을지도 몰라요.
                                """.trimIndent(),
                    leftHandler = Pair("지울게요", object: OptionalClickListener
                    {
                        override fun invoke(dialog: OptionalDialog)
                        {
                            dialog.dismiss()
                        }
                    }),
                    rightHandler = Pair("유지할게요", object: OptionalClickListener
                    {
                        override fun invoke(dialog: OptionalDialog)
                        {
                            dialog.dismiss()
                        }
                    })).show(requireActivity().supportFragmentManager, "")
            }

            override fun onEnterParcelDetailClicked(view: View, type: InquiryStatusEnum, parcelId: Int)
            {
                super.onEnterParcelDetailClicked(view, type, parcelId)

                TabCode.INQUIRY_DETAIL.FRAGMENT = ParcelDetailView.newInstance(parcelId)
                FragmentManager.add(requireActivity(), TabCode.INQUIRY_DETAIL, InquiryMainFragment.viewId)
            }

            override fun onUpdateParcelAliasClicked(view: View, type: InquiryStatusEnum, parcelId: Int)
            {
                super.onUpdateParcelAliasClicked(view, type, parcelId)

                val edit = MutableLiveData<String>()

                AlertUtil.updateValueDialog(requireContext(), "물품명을 입력해주세요.", Pair("확인", View.OnClickListener {
                    edit.observe(requireActivity()) { parcelAlias ->
                        vm.onUpdateParcelAlias(parcelId, parcelAlias)
                        AlertUtil.onDismiss()
                    }
                }), Pair("취소", null), Function {
                    edit.value = it
                })
            }

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
                                                                 {
                                                                     //삭제하기
                                                                     //                                                                     vm.onOpenDeleteView()
                                                                     menuPopUpWindow?.dismiss()
                                                                 }

                                                                 override fun refreshItems(v: View)
                                                                 {
                                                                     // 새로고침
                                                                     vm.syncParcelsByOngoing()
                                                                     menuPopUpWindow?.dismiss()
                                                                 }

                                                                 override fun help(v: View)
                                                                 {
                                                                     // 도움말
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
                                SopoLog.d("이건가 시발 ")
                                vm.changeCompletedParcelHistoryDate(year = year)

                                historyPopUpWindow?.dismiss()
                            }
                        }

                    popupMenuListAdapter.setHistoryPopUpItemOnclick(historyPopUpItemOnClick)

                    it.scrollBarFadeDuration = 800
                }
            }
        historyPopUpWindow = if(histories.size > 2)
        {
            PopupWindow(historyPopUpView.root, SizeUtil.changeDpToPx(binding.root.context, 160F), SizeUtil.changeDpToPx(binding.root.context, 35 * 6F), true).apply {
                CoroutineScope(Dispatchers.Main).launch {
                    showAsDropDown(anchorView, -80, 0, Gravity.CENTER)
                }
            }
        }
        else
        {
            PopupWindow(historyPopUpView.root, SizeUtil.changeDpToPx(binding.root.context, 160F), ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {

                CoroutineScope(Dispatchers.Main).launch {
                    showAsDropDown(anchorView, -80, 0, Gravity.CENTER)
                }

            }
        }
    }

    private fun setListener()
    {
        // 당겨서 새로고침 !
        binding.swipeRefresh.setOnRefreshListener {

            if(!refreshDelay)
            {
                refreshDelay = true

                when(vm.getCurrentScreenStatus())
                {
                    InquiryStatusEnum.COMPLETE ->
                    {
                        vm.refreshCompleteParcels()
                    }
                    InquiryStatusEnum.ONGOING ->
                    {
                        vm.syncParcelsByOngoing()
                    }
                }

                //5초후에 실행
                Timer().schedule(object: TimerTask()
                                 {
                                     override fun run()
                                     {
                                         CoroutineScope(Dispatchers.Main).launch {
                                             refreshDelay = false
                                         }
                                     }
                                 }, 5000)

                binding.swipeRefresh.isRefreshing = false

                return@setOnRefreshListener
            }

            Toast.makeText(requireContext(), "5초 후에 다시 새로고침을 시도해주세요.", Toast.LENGTH_LONG).show()

            binding.swipeRefresh.isRefreshing = false
        }


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

    //팝업 메뉴에서 '삭제하기'가 선택되었을때 화면 세팅
    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    /*    private fun viewSettingForPopupMenuDelete()
        {
            binding.tvTitle.visibility = INVISIBLE
            binding.linearStatusSelector.visibility = INVISIBLE
            binding.ivPopMenu.visibility = INVISIBLE
            binding.linearMoreViewParent.visibility = GONE

            binding.vMoreView.visibility = INVISIBLE

        }*/

    // X 버튼으로 '삭제하기 취소'가 되었을때 화면 세팅
    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    /*   private fun viewSettingForPopupMenuDeleteCancel()
       {
           binding.tvTitle.visibility = VISIBLE
           binding.linearStatusSelector.visibility = VISIBLE
           binding.ivPopMenu.visibility = VISIBLE


           // 삭제하기 취소가 되었을때 화면의 리스트들을 앱이 켜졌을때 처럼 초기화 시켜준다.( '더보기'가 눌렸었는지 아니면 내가 전에 리스트들의 스크롤을 얼마나 내렸는지를 일일이 알고 있기 힘들기 때문에)
           viewSettingForSoonArrivalList(soonArrivalParcelAdapter.getListSize())
           viewSettingForRegisteredList(registeredParcelAdapter.getListSize())
       }
   */
    private fun setDefaultMonthSelector()
    {
        val (clickable, textColor, font) = Triple(first = false, second = ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_300), third = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium))


        binding.tvJan.apply {
            setTextColor(textColor)
            typeface = font
            isClickable = clickable
            isFocusable = clickable
            background = null


            binding.tvFeb.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }


            binding.tvMar.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                isFocusable = clickable
                background = null
            }

            binding.tvApr.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }

            binding.tvMay.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }

            binding.tvJun.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }

            binding.tvJul.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }

            binding.tvAug.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }

            binding.tvSep.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }


            binding.tvOct.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }


            binding.tvNov.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }


            binding.tvDec.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                background = null
            }


        }
    }

    private fun updateCompleteUI()
    {
        val onGlobalLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener
        {
            override fun onGlobalLayout()
            {
                SopoLog.d("onGlobalLayout 호출")
                val yearSpinnerHeight: Int = binding.linearOutYearSpinner.height

                SopoLog.d("yearSpinnerHeight $yearSpinnerHeight")

                (binding.linearMonthSelector.layoutParams as FrameLayout.LayoutParams).apply {
                    topMargin = yearSpinnerHeight
                }

                val monthSelectorHeight = binding.linearMonthSelector.height

                SopoLog.d("monthSelectorHeight $monthSelectorHeight")

                (binding.vEmpty2.layoutParams as LinearLayout.LayoutParams).apply {
                    this.topMargin = yearSpinnerHeight
                    this.height = monthSelectorHeight
                }

                binding.frameMainCompleteInquiry.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }

        binding.frameMainCompleteInquiry.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }
}