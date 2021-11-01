package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.databinding.FragmentInquiryReBinding
import com.delivery.sopo.databinding.PopupMenuViewBinding
import com.delivery.sopo.enums.InquiryItemTypeEnum
import com.delivery.sopo.enums.InquiryStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnParcelClickListener
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.models.UpdateAliasRequest
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.inquiry.InquiryMenuItem
import com.delivery.sopo.models.mapper.MenuMapper
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.views.adapter.InquiryListAdapter
import com.delivery.sopo.views.adapter.PopupMenuListAdapter
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.function.Function
import kotlin.NoSuchElementException
import kotlin.system.exitProcess


class InquiryFragment: BaseFragment<FragmentInquiryReBinding, InquiryViewModel>()
{

    override val layoutRes: Int = R.layout.fragment_inquiry_re

    override val vm: InquiryViewModel by viewModel()

    private val parentView: MainView by lazy {
        activity as MainView
    }

    // 곧 도착 택배 리스트 adapter, 등록된 택배(진행 중) 리스트 adapter, 도착 완료된 택배 리스트 adapter
    private lateinit var soonArrivalParcelAdapter: InquiryListAdapter
    private lateinit var registeredParcelAdapter: InquiryListAdapter
    private lateinit var completedParcelAdapter: InquiryListAdapter

    private var menuPopUpWindow: PopupWindow? = null
    private var historyPopUpWindow: PopupWindow? = null

    private var progressBar: CustomProgressBar? = null

    private var refreshDelay: Boolean = false

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        onSOPOBackPressedListener = object: OnSOPOBackPressListener{
            override fun onBackPressedInTime()
            {
                Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000)
                    .apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }.show()
            }

            override fun onBackPressedOutTime()
            {
                ActivityCompat.finishAffinity(parentView)
                exitProcess(0)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        setAdapters()
        setListener()
        binding.ivPopMenu.setOnClickListener {
            openInquiryMenu(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initUI()
    {
        SopoLog.d("base fragment - initUI Call2")


        setAdapters()

        binding.vEmpty2.setOnTouchListener { v, event ->
            return@setOnTouchListener binding.linearMonthSelector.dispatchTouchEvent(event)
        }

        binding.nestScrollViewComplete.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if(scrollY > 0 && oldScrollY == 0) vm.isClickableMonths = false
            else if(scrollY == 0 && oldScrollY > 0) vm.isClickableMonths = true
        }

        updateCompleteUI()
    }

    override fun setAfterSetUI()
    {
    }

    private fun getAdapter(inquiryItemTypeEnum: InquiryItemTypeEnum): InquiryListAdapter
    {
        SopoLog.d("Test2. getAdapter")

        return InquiryListAdapter(parcelType = inquiryItemTypeEnum).apply {
            this.setOnParcelClickListener(getParcelClicked())
        }
    }

    private fun setAdapters()
    {
        getAdapter(InquiryItemTypeEnum.Soon).let { adapter ->
            soonArrivalParcelAdapter = adapter
            binding.recyclerviewSoonArrival.adapter = soonArrivalParcelAdapter
        }
        getAdapter(InquiryItemTypeEnum.Registered).let { adapter ->
            registeredParcelAdapter = adapter
            binding.recyclerviewRegisteredParcel.adapter = registeredParcelAdapter
        }
        getAdapter(InquiryItemTypeEnum.Complete).let { adapter ->
            completedParcelAdapter = adapter
            binding.recyclerviewCompleteParcel.adapter = completedParcelAdapter
        }
    }

    private fun activateInquiryStatus(tv: TextView){
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.MAIN_WHITE))
        tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.border_all_rounded_light_black)
        tv.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
    }

    private fun inactivateInquiryStatus(tv: TextView){
        tv.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
        tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.border_all_rounded_color_gray_400)
    }

    override fun setObserve()
    {
        /*parentView.currentPage.observe(requireActivity(), Observer {
            if(it != null && it == TabCode.secondTab)
            {
                requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
            }
        })*/

        // 배송중 , 등록된 택배 리스트
        vm.ongoingList.observe(requireActivity(), Observer { list ->

            soonArrivalParcelAdapter.separateDeliveryListByStatus(list)
            registeredParcelAdapter.separateDeliveryListByStatus(list)

            viewSettingForSoonArrivalList(soonArrivalParcelAdapter.getListSize())
            viewSettingForRegisteredList(registeredParcelAdapter.getListSize())
        })


        // '배송 중' 또는 '배송 완료' 화면에 따른 화면 세팅
        // TODO 데이터 바인딩으로 처리할 수 있으면 처리하도록 수정해야함.
        vm.inquiryStatus.observe(requireActivity()) {

            SopoLog.d("Inquiry Status -> ${it}")

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

        // '더 보기'로 아이템들을 숨기는 것을 해제하여 모든 아이템들을 화면에 노출시킨다.
        vm.isMoreView.observe(requireActivity(), Observer {
            if(it)
            {
                // '곧 도착' 리스트뷰는 2개 이상의 데이터는 '더 보기'로 숨겨져 있기 때문에 어덥터에 모든 데이터를 표출하라고 지시한다.
                soonArrivalParcelAdapter.isFullListItem(true)

                // 모든 아이템들을 노출 시켰을때 화면 세팅
                binding.linearMoreView.visibility = VISIBLE
                binding.tvMoreView.text = ""
                binding.imageArrow.setBackgroundResource(R.drawable.ic_up_arrow)
            }
            else
            {
                // '곧 도착' 리스트뷰의 2개 이상의 데이터가 존재할떄 '더 보기'로 숨기라고 지시한다.
                soonArrivalParcelAdapter.isFullListItem(false)

                // 제한된 아이템들을 노출 시킬때의 화면 세팅
                binding.linearMoreView.visibility = VISIBLE
                binding.tvMoreView.text = "더 보기"
                binding.imageArrow.setBackgroundResource(R.drawable.ic_down_arrow)
            }
        })


        /** 배송 완료
         */
        // 배송완료 리스트에서 해당 년월에 속해있는 택배들을 전부 삭제했을 때는 서버로 통신해서 새로고침하면 안돼고 무조건 로컬에 있는 데이터로 새로고침 해야한다.
        // (서버로 통신해서 새로고침하면 서버에 있는 데이터(우선순위가 높음)로 덮어써버리기 때문에 '삭제취소'를 통해 복구를 못함..)
        // (새로고침하면 내부에 저장된 '삭제할 데이터'들을 모두 서버로 통신하여 Remote database에서도 삭제처리(데이터 동기화)를 하고 나서 새로운 데이터를 받아옴)
/*        vm.refreshCompleteListByOnlyLocalData.observe(requireActivity(), Observer {
            if(it > 0)
            {
                vm.refreshCompleteListByOnlyLocalData()
            }
        })*/

        // 배송완료 리스트.
        vm.completeList.observe(requireActivity(), Observer { list ->
            SopoLog.d("!!! 완료 택배 갯수 ${list.size}")

            val ml = list.toMutableList()
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)
            ml.addAll(list)

            list.sortByDescending { it.parcelDTO.arrivalDte }
            completedParcelAdapter.notifyChanged(ml)
        })


        // 배송완료 화면에서 표출 가능한 년월 리스트
        vm.histories.observe(requireActivity()) { dates ->

            if(dates.isEmpty())
            {
                SopoLog.d("완료 택배가 없습니다.")
                return@observe
            }

            val latestDate = try {
                dates.first { date -> date.count > 0}
            } catch(e:NoSuchElementException){
                dates.first()
            }

            vm.updateCompletedParcelCalendar(latestDate.year)

            // TODO 다른 곳으로 빼야할듯
            binding.constraintYearSpinner.setOnClickListener { v ->
                drawCompletedParcelHistoryPopMenu(v, dates)
            }
        }

        vm.monthsOfCalendar.observe(requireActivity()) { list ->

            SopoLog.d("currentCompleteParcelMonths Observing...")

            setDefaultMonthSelector()
            vm.selectedDate.postValue("")

            val reversedList = list.reversed()

            reversedList.forEach {

                if(it.item.count > 0 && it.isSelect)
                {
                    vm.selectedDate.postValue("${it.item.year}년 ${it.item.month}월")
                }

                val (clickable, textColor, font) = if(it.item.count > 0)
                {
                    Triple(first = true,
                           second = if(it.isSelect) ContextCompat.getColor(requireContext(),
                                                                           R.color.MAIN_WHITE)
                           else ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800),
                           third = ResourcesCompat.getFont(requireContext(),
                                                           R.font.spoqa_han_sans_neo_bold))
                }
                else
                {
                    Triple(first = false, second = ContextCompat.getColor(requireContext(),
                                                                          R.color.COLOR_GRAY_300),
                           third = ResourcesCompat.getFont(requireContext(),
                                                           R.font.spoqa_han_sans_neo_regular))
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
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
                            background = if(it.isSelect) ContextCompat.getDrawable(requireContext(),
                                                                                   R.drawable.oval_24dp_gray_scale)
                            else null
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

    private fun getParcelClicked(): OnParcelClickListener
    {
        return object: OnParcelClickListener
        {
            override fun onParcelClicked(view: View, type: Int, parcelId: Int)
            {
                TabCode.INQUIRY_DETAIL.FRAGMENT = ParcelDetailView.newInstance(parcelId)
                FragmentManager.move(requireActivity(), TabCode.INQUIRY_DETAIL,
                                     InquiryMainFrame.viewId)
            }

            override fun onParcelLongClicked(view: View, type: Int, parcelId: Int)
            {
                val edit = MutableLiveData<String>()

                AlertUtil.updateValueDialog(requireContext(), "물품명을 입력해주세요.",
                                            Pair("확인", View.OnClickListener {
                                                edit.observe(requireActivity(), Observer { alias ->

                                                    val updateAliasRequest =
                                                        UpdateAliasRequest(parcelId = parcelId,
                                                                           alias = alias)

                                                    vm.onUpdateParcelAlias(updateAliasRequest)

                                                    AlertUtil.onDismiss()

                                                    if(type == 0)
                                                    {
                                                        vm.refreshOngoingParcels()
                                                    }
                                                    else
                                                    {
                                                        vm.refreshCompleteParcels()
                                                    }
                                                })
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
                val popupMenuListAdapter = PopupMenuListAdapter(
                    MenuMapper.menuToMenuItemList(menu) as MutableList<InquiryMenuItem>)


                v.recyclerviewInquiryPopupMenu.also {
                    it.adapter = popupMenuListAdapter
                    val dividerItemDecoration =
                        DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                    dividerItemDecoration.setDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.line_divider)!!)
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
                                                                     vm.refreshOngoingParcels()
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
            PopupWindow(popUpView.root, SizeUtil.changeDpToPx(binding.root.context, 175F),
                        ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                showAsDropDown(anchorView)
            }

    }

    // 배송완료 화면에서 년/월을 눌렀을 시 팝업 메뉴가 나온다.
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun drawCompletedParcelHistoryPopMenu(anchorView: View, histories: List<CompletedParcelHistory>)
    {
        val historyPopUpView: PopupMenuViewBinding = PopupMenuViewBinding.inflate(LayoutInflater.from(context)).also { v ->

                val inquiryMenuItems = MenuMapper.completeParcelStatusDTOToMenuItem(histories) as MutableList<InquiryMenuItem>

                val popupMenuListAdapter = PopupMenuListAdapter(inquiryMenuItems)

                v.recyclerviewInquiryPopupMenu.also {
                    it.adapter = popupMenuListAdapter
                    val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.line_divider)!!)
                    it.addItemDecoration(dividerItemDecoration)

                    popupMenuListAdapter.setHistoryPopUpItemOnclick(object: PopupMenuListAdapter.HistoryPopUpItemOnclick
                                                                    {
                                                                        override fun changeTimeCount(v: View, year: String)
                                                                        {
                                                                            vm.changeCompletedParcelHistoryDate(year = year)

                                                                            historyPopUpWindow?.dismiss()
                                                                        }
                                                                    })

                    it.scrollBarFadeDuration = 800
                }
            }
        historyPopUpWindow = if(histories.size > 2)
        {
            PopupWindow(historyPopUpView.root, SizeUtil.changeDpToPx(binding.root.context, 160F),
                        SizeUtil.changeDpToPx(binding.root.context, 35 * 6F), true).apply {
                showAsDropDown(anchorView)
            }
        }
        else
        {
            PopupWindow(historyPopUpView.root, SizeUtil.changeDpToPx(binding.root.context, 160F),
                        ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                showAsDropDown(anchorView)
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
                        vm.refreshOngoingParcels()
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
                    val date = vm.selectedDate.value?.replace("년 ", "")?.replace("월", "")

                    SopoLog.d("완료 택배 RecyclerView $date")

                    CoroutineScope(Dispatchers.IO).launch {
                        vm.getCompleteListWithPaging(MenuMapper.titleToInquiryDate(date ?: return@launch))
                    }
                }
            }
        }

        binding.recyclerviewCompleteParcel.addOnScrollListener(onScrollListener)
    }

    // '곧 도착' 리스트의 아이템의 개수에 따른 화면세팅
    private fun viewSettingForSoonArrivalList(listSize: Int)
    {
        when(listSize)
        {
            // 아이템의 개수가 0개일때
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
            }
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
    private fun viewSettingForPopupMenuDelete()
    {
        binding.tvTitle.visibility = INVISIBLE
        binding.linearStatusSelector.visibility = INVISIBLE
        binding.ivPopMenu.visibility = INVISIBLE
        binding.linearMoreViewParent.visibility = GONE

        binding.vMoreView.visibility = INVISIBLE

    }

    // X 버튼으로 '삭제하기 취소'가 되었을때 화면 세팅
    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    private fun viewSettingForPopupMenuDeleteCancel()
    {
        binding.tvTitle.visibility = VISIBLE
        binding.linearStatusSelector.visibility = VISIBLE
        binding.ivPopMenu.visibility = VISIBLE



        // 삭제하기 취소가 되었을때 화면의 리스트들을 앱이 켜졌을때 처럼 초기화 시켜준다.( '더보기'가 눌렸었는지 아니면 내가 전에 리스트들의 스크롤을 얼마나 내렸는지를 일일이 알고 있기 힘들기 때문에)
        viewSettingForSoonArrivalList(soonArrivalParcelAdapter.getListSize())
        viewSettingForRegisteredList(registeredParcelAdapter.getListSize())
    }

    fun setDefaultMonthSelector()
    {
        val (clickable, textColor, font) = Triple(first = false,
                                                  second = ContextCompat.getColor(requireContext(),
                                                                                  R.color.COLOR_GRAY_300),
                                                  third = ResourcesCompat.getFont(requireContext(),
                                                                                  R.font.spoqa_han_sans_neo_regular))


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

    override fun onResume()
    {
        super.onResume()

        SopoLog.d("뭐지 시발 진짜로 -> ${binding.nestedScrollView.visibility == View.VISIBLE}")
    }

    private fun updateCompleteUI()
    {
        val onGlobalLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener
        {
            override fun onGlobalLayout()
            {
                val yearSpinnerHeight: Int = binding.linearOutYearSpinner.height

                (binding.linearMonthSelector.layoutParams as FrameLayout.LayoutParams).apply {
                    topMargin = yearSpinnerHeight
                }

                val monthSelectorHeight = binding.linearMonthSelector.height

                (binding.vEmpty2.layoutParams as LinearLayout.LayoutParams).apply {
                    this.topMargin = yearSpinnerHeight
                    this.height = monthSelectorHeight
                }

                binding.frameComplete.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }

        binding.frameComplete.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }
}