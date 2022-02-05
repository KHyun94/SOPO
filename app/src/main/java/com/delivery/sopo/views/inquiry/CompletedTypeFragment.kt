package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.databinding.FragmentCompletedTypeBinding
import com.delivery.sopo.databinding.FragmentOngoingTypeBinding
import com.delivery.sopo.databinding.PopupMenuViewBinding
import com.delivery.sopo.enums.*
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.interfaces.listener.ParcelEventListener
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.inquiry.InquiryMenuItem
import com.delivery.sopo.models.mapper.MenuMapper
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.viewmodels.inquiry.CompletedTypeViewModel
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.views.adapter.InquiryListAdapter
import com.delivery.sopo.views.adapter.PopupMenuListAdapter
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.dialog.OptionalDialog
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.system.exitProcess

class CompletedTypeFragment: BaseFragment<FragmentCompletedTypeBinding, CompletedTypeViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_completed_type
    override val vm: CompletedTypeViewModel by viewModel()
    override val mainLayout: View by lazy { binding.swipeLayoutMainCompleted }

    private lateinit var completedParcelAdapter: InquiryListAdapter

    private var historyPopUpWindow: PopupWindow? = null

    private var refreshDelay: Boolean = false

    private val parentView: MainView by lazy { activity as MainView }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        onSOPOBackPressedListener = object: OnSOPOBackPressListener
        {
            override fun onBackPressedInTime()
            {
                Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다. 완료형", 2000)
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

    var isRefresh = true
    var returnType = 0

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        isRefresh = bundle.getBoolean("IS_REFRESH")
        returnType = bundle.getInt("RETURN_TYPE")
    }

    override fun setBeforeBinding()
    {

    }

    override fun setAfterBinding()
    {
        setAdapters()
        setListener()


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

        getAdapter(InquiryItemTypeEnum.Complete).let { adapter ->
            completedParcelAdapter = adapter
            binding.recyclerviewCompleteParcel.adapter = completedParcelAdapter
            val animator = binding.recyclerviewCompleteParcel.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }
    }
    override fun onResume()
    {
        super.onResume()
        SopoLog.d("onResume Complete")
        Toast.makeText(requireContext(), "TestTest Complete", Toast.LENGTH_SHORT).show()

        SopoLog.d("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")

        parentView.onBackPressedDispatcher.addCallback(parentView, onBackPressedCallback)
    }
    override fun setObserve()
    {
        super.setObserve()

        activity ?: return

        parentView.currentPage.observe(this) {
            if(it != 1) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        // 배송완료 리스트.
        vm.completeList.observe(requireActivity()) { list ->
            completedParcelAdapter.notifyChanged(list)
        }

        // 배송완료 화면에서 표출 가능한 년월 리스트
        vm.histories.observe(requireActivity()) { dates ->

            CoroutineScope(Dispatchers.Main).launch {

                if(dates.isEmpty())
                {
                    SopoLog.d("완료 택배가 없습니다.")

                    binding.includeCompleteNoItem.visible = View.VISIBLE

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

            override fun onUpdateParcelAliasClicked(view: View, type: InquiryStatusEnum, parcelId: Int)
            {
                super.onUpdateParcelAliasClicked(view, type, parcelId)

                val edit = MutableLiveData<String>()

                AlertUtil.updateValueDialog(requireContext(), "물품명을 입력해주세요.", Pair("확인", View.OnClickListener {
                    edit.observe(requireActivity()) { parcelAlias ->
                        vm.onUpdateParcelAlias(parcelId, parcelAlias)
                        AlertUtil.onDismiss()
                    }
                }), Pair("취소", null)) {
                    edit.value = it
                }
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
    { // 당겨서 새로고침 !
        binding.swipeLayoutMainCompleted.setOnRefreshListener {

            if(!refreshDelay)
            {
                refreshDelay = true

                vm.refreshCompleteParcels()

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