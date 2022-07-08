package com.delivery.sopo.presentation.views.inquiry

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.delivery.sopo.R
import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory
import com.delivery.sopo.databinding.FragmentCompletedTypeBinding
import com.delivery.sopo.databinding.PopupMenuViewBinding
import com.delivery.sopo.enums.*
import com.delivery.sopo.interfaces.OnPageSelectListener
import com.delivery.sopo.interfaces.OnTapReselectListener
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.interfaces.listener.ParcelEventListener
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.inquiry.InquiryMenuItem
import com.delivery.sopo.models.mapper.MenuMapper
import com.delivery.sopo.presentation.consts.IntentConst
import com.delivery.sopo.presentation.viewmodels.inquiry.CompletedTypeViewModel
import com.delivery.sopo.presentation.views.adapter.InquiryListAdapter
import com.delivery.sopo.presentation.views.adapter.PopupMenuListAdapter
import com.delivery.sopo.presentation.views.main.MainView
import com.delivery.sopo.util.*
import com.delivery.sopo.util.ui_util.UpdateValueDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class CompletedTypeFragment: BaseFragment<FragmentCompletedTypeBinding, CompletedTypeViewModel>(),
        OnTapReselectListener
{
    override val layoutRes: Int = R.layout.fragment_completed_type
    override val vm: CompletedTypeViewModel by viewModel()
    override val mainLayout: View by lazy { binding.swipeLayoutMainCompleted }

    private val parentView: MainView by lazy { activity as MainView }

    private lateinit var completedParcelAdapter: InquiryListAdapter

    private var historyPopUpWindow: PopupWindow? = null

    private var refreshDelay: Boolean = false

    private lateinit var onPageSelectListener: OnPageSelectListener

    private var scrollStatus: ScrollStatusEnum = ScrollStatusEnum.TOP

    val broadcastReceiver: BroadcastReceiver = object: BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?)
        {
            intent ?: return

            SopoLog.d("Registered Action ${intent.action}")

            when(intent.action)
            {
                IntentConst.Action.REGISTERED_COMPLETED_PARCEL ->
                {
                    val data = intent.getStringExtra("REGISTERED_DATE") ?: return

                    val date = DateUtil.convertDate(data, DateUtil.DATE_TIME_TYPE_DEFAULT) ?: return
                    val calendar = Calendar.getInstance()
                    calendar.time = date

                    val year = calendar.get(Calendar.YEAR).toString()
                    val month = (calendar.get(Calendar.MONTH) + 1)

                    vm.getActivateMonths()

                    Handler(Looper.getMainLooper()).postDelayed({
                        vm.changeCompletedParcelHistoryDate(year = year)
                        vm.onMonthClicked(month)
                    }, 500)
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
            addAction(IntentConst.Action.REGISTERED_COMPLETED_PARCEL)
        }

        parentView.registerReceiver(broadcastReceiver, filter)

        if(scrollStatus != ScrollStatusEnum.TOP)
        {
            onPageSelectListener.onChangeTab(TabCode.INQUIRY_COMPLETE)
        }
        else
        {
            onPageSelectListener.onChangeTab(null)
        }

        parentView.onReselectedTapClickListener = object: () -> Unit
        {
            override fun invoke()
            {
                if(scrollStatus == ScrollStatusEnum.TOP) return
                binding.nestSvMainCompleted.scrollTo(0, 0)
            }
        }
    }

    override fun onPause()
    {
        super.onPause()
        parentView.unregisterReceiver(broadcastReceiver)
    }

    private fun setOnMainBridgeListener(context: Context)
    {
        onPageSelectListener = context as OnPageSelectListener
    }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent()
        {
            override fun onBackPressedInTime()
            {
                Snackbar.make(parentView.binding.layoutMain, "완료 한번 더 누르시면 앱이 종료됩니다.", 2000)
                    .apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }
                    .show()
            }

            override fun onBackPressedOutTime()
            {
                exit()
            }
        }

        setOnMainBridgeListener(context = requireContext())
    }

    override fun setAfterBinding()
    {
        setActivateMonthCalendar()

        setAdapters()
        setListener()
    }


    override fun setObserve()
    {
        super.setObserve()

        activity ?: return

        parentView.getCurrentPage().observe(this) {
            if(it != 1) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        // 배송완료 리스트.
        vm.completeList.observe(requireActivity()) { list ->

            completedParcelAdapter.separateDelivered(list.toMutableList())
        }

        // 배송완료 화면에서 표출 가능한 년월 리스트
        vm.histories.observe(requireActivity()) { dates ->

            vm.initPage()

            if(dates.isEmpty())
            {
                binding.includeCompleteNoItem.visible = View.VISIBLE
                return@observe
            }

            SopoLog.d("Completed Parcel [size:${dates.size}]")

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
            binding.constraintInnerYearSpinner.setOnClickListener { v ->
                drawCompletedParcelHistoryPopMenu(v, dates)
            }
        }

        vm.monthsOfCalendar.observe(requireActivity()) { list ->

            setDefaultMonthSelector()
            vm.selectedDate.postValue("")

            val reversedList = list.reversed()

            reversedList.forEach { historySelectItem ->

                val item = historySelectItem.item
                val isSelect = historySelectItem.isSelect

                if(item.count > 0 && isSelect)
                {
                    val selectedDate = with(item) { "${year}년 ${month}월" }
                    vm.selectedDate.postValue(selectedDate)
                }

                val (clickable, textColor, font) = if(item.count > 0)
                {
                    val textColor =
                        if(isSelect) ContextCompat.getColor(requireContext(), R.color.MAIN_WHITE)
                        else ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800)

                    val font = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)

                    Triple(first = true, second = textColor, third = font)
                }
                else
                {
                    val textColor = ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_300)
                    val font = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)

                    Triple(first = false, second = textColor, third = font)
                }

                when(item.month)
                {
                    "01" ->
                    {
                        setMonthItem(tv = binding.tvJan, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "02" ->
                    {
                        setMonthItem(tv = binding.tvFeb, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "03" ->
                    {
                        setMonthItem(tv = binding.tvMar, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "04" ->
                    {
                        setMonthItem(tv = binding.tvApr, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "05" ->
                    {
                        setMonthItem(tv = binding.tvMay, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "06" ->
                    {
                        setMonthItem(tv = binding.tvJun, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "07" ->
                    {
                        setMonthItem(tv = binding.tvJul, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "08" ->
                    {
                        setMonthItem(tv = binding.tvAug, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "09" ->
                    {
                        setMonthItem(tv = binding.tvSep, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "10" ->
                    {
                        setMonthItem(tv = binding.tvOct, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "11" ->
                    {
                        setMonthItem(tv = binding.tvNov, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                    "12" ->
                    {
                        setMonthItem(tv = binding.tvDec, font = font, textColor = textColor, clickable = clickable, isSelected = isSelect)
                    }
                }
            }
        }

        vm.selectedDate.observe(requireActivity()) { date ->
            if(date.isEmpty()) return@observe
            val searchDate = date.replace("년 ", "").replace("월", "")
            SopoLog.d("DATE => $searchDate")
            vm.refreshCompleteParcelsByDate(searchDate)
        }
    }

    private fun getAdapter(inquiryItemTypeEnum: InquiryItemTypeEnum): InquiryListAdapter
    {
        return InquiryListAdapter(parcelType = inquiryItemTypeEnum).apply {
            this.setOnParcelClickListener(getParcelClicked())
        }
    }

    private fun setAdapters()
    {
        getAdapter(InquiryItemTypeEnum.Complete).let { adapter ->
            completedParcelAdapter = adapter
            binding.recyclerviewCompleteParcel.adapter = completedParcelAdapter
            val animator = binding.recyclerviewCompleteParcel.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }

        ViewCompat.setNestedScrollingEnabled(binding.recyclerviewCompleteParcel, false)
    }

    // 활성화된 월 선택 세팅 setActivateMonthCalendar
    private fun setActivateMonthCalendar()
    {
        SopoLog.d("호출")

        val onGlobalLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener
        {
            override fun onGlobalLayout()
            { // 'year spinner'높이 수치만큼 'month sector'의 상단 공백을 생성
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

            override fun onUpdateParcelAliasClicked(view: View, type: InquiryStatusEnum, parcelId: Int)
            {
                super.onUpdateParcelAliasClicked(view, type, parcelId)

                UpdateValueDialog{ alias ->
                    SopoLog.d("TEST INPUT DATA $alias")
                    vm.updateParcelAlias(parcelId, alias)
                }.show(childFragmentManager, "")
            }

        }
    }

    // 배송완료 화면에서 년/월을 눌렀을 시 팝업 메뉴가 나온다.
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun drawCompletedParcelHistoryPopMenu(anchorView: View, histories: List<DeliveredParcelHistory>)
    {
        val historyPopUpView: PopupMenuViewBinding =
            PopupMenuViewBinding.inflate(LayoutInflater.from(context))

        val inquiryMenuItems =
            MenuMapper.completeParcelStatusDTOToMenuItem(histories) as MutableList<InquiryMenuItem>

        val popupMenuListAdapter = PopupMenuListAdapter(inquiryMenuItems)

        historyPopUpView.recyclerviewInquiryPopupMenu.also {
            it.adapter = popupMenuListAdapter
            val dividerItemDecoration =
                DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            dividerItemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.line_divider)!!)
            it.addItemDecoration(dividerItemDecoration)

            val historyPopUpItemOnClick = object: PopupMenuListAdapter.HistoryPopUpItemOnclick
            {
                override fun changeTimeCount(v: View, year: String)
                {
                    vm.changeCompletedParcelHistoryDate(year = year)
                    historyPopUpWindow?.dismiss()
                }
            }

            popupMenuListAdapter.setHistoryPopUpItemOnclick(historyPopUpItemOnClick)

            it.scrollBarFadeDuration = 800
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener()
    { // 당겨서 새로고침 !
        binding.swipeLayoutMainCompleted.setOnRefreshListener {

            if(!refreshDelay)
            {
                refreshDelay = true

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

                binding.swipeLayoutMainCompleted.isRefreshing = false

                return@setOnRefreshListener
            }

            Toast.makeText(requireContext(), "5초 후에 다시 새로고침을 시도해주세요.", Toast.LENGTH_LONG).show()

            binding.swipeLayoutMainCompleted.isRefreshing = false
        }


        val onScrollListener = object: RecyclerView.OnScrollListener()
        {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
            {
                super.onScrollStateChanged(recyclerView, newState)

                val storedParcels = completedParcelAdapter.getListSize()
                val hasNextPage = storedParcels > 0 && storedParcels % 10 == 0

                val linearLayoutManager =
                    (binding.recyclerviewCompleteParcel.layoutManager as LinearLayoutManager)
                val lastCompletelyVisibleItemPosition =
                    linearLayoutManager.findLastCompletelyVisibleItemPosition()

                val isLastCompletelyVisibleItemPosition =
                    lastCompletelyVisibleItemPosition % 10 == 9 || lastCompletelyVisibleItemPosition % 10 == 0
                val isEndOfList = !recyclerView.canScrollVertically(1)

                if(!hasNextPage || !isLastCompletelyVisibleItemPosition) return
                val searchDate =
                    vm.selectedDate.value?.replace("년 ", "")?.replace("월", "") ?: return
                vm.refreshCompleteParcelsByDate(searchDate)
            }
        }

        binding.recyclerviewCompleteParcel.addOnScrollListener(onScrollListener)

        binding.vInnerCompletedSpace.setOnTouchListener { _, event ->
            return@setOnTouchListener binding.linearMainMonthSelector.dispatchTouchEvent(event)
        }

        binding.nestSvMainCompleted.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            if(scrollY > 0)
            {
                scrollStatus = ScrollStatusEnum.MIDDLE
                onPageSelectListener.onChangeTab(TabCode.INQUIRY_COMPLETE)
            }
            else
            {
                scrollStatus = ScrollStatusEnum.TOP
                onPageSelectListener.onChangeTab(null)
            }

            if(scrollY > 0 && oldScrollY == 0) vm.isMonthClickable = false
            else if(scrollY == 0 && oldScrollY > 0) vm.isMonthClickable = true
        }

    }

    private fun setDefaultMonthSelector()
    {
        val (clickable, textColor, font) = Triple(first = false, second = ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_300), third = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium))

        setMonthItem(tv = binding.tvJan, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvFeb, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvMar, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvApr, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvMay, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvJun, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvJul, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvAug, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvSep, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvOct, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvNov, font = font, textColor = textColor, clickable = clickable)
        setMonthItem(tv = binding.tvDec, font = font, textColor = textColor, clickable = clickable)
    }

    private fun setMonthItem(tv: TextView, font: Typeface?, textColor: Int, clickable: Boolean, isSelected: Boolean = false) =
        CoroutineScope(Dispatchers.Main).launch {
            tv.apply {
                setTextColor(textColor)
                typeface = font
                isClickable = clickable
                isFocusable = clickable
                this.isSelected = isSelected
            }
        }

    override fun onReselect()
    {
        Toast.makeText(requireContext(), "테스트 !!", Toast.LENGTH_LONG).show()
    }
}