package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.database.room.dto.CompleteParcelStatusDTO
import com.delivery.sopo.databinding.FragmentInquiryReBinding
import com.delivery.sopo.databinding.PopupMenuViewBinding
import com.delivery.sopo.enums.InquiryItemTypeEnum
import com.delivery.sopo.enums.ScreenStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnParcelClickListener
import com.delivery.sopo.models.UpdateAliasRequest
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
import org.koin.android.ext.android.bind
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.function.Function
import kotlin.system.exitProcess


class InquiryFragment: Fragment()
{
    private lateinit var parentView: MainView
    private lateinit var binding: FragmentInquiryReBinding

    private val vm: InquiryViewModel by viewModel()

    // 곧 도착 택배 리스트 adapter, 등록된 택배(진행 중) 리스트 adapter, 도착 완료된 택배 리스트 adapter
    private lateinit var soonArrivalParcelAdapter: InquiryListAdapter
    private lateinit var registeredParcelAdapter: InquiryListAdapter
    private lateinit var completedParcelAdapter: InquiryListAdapter

    private var menuPopUpWindow: PopupWindow? = null
    private var historyPopUpWindow: PopupWindow? = null

    private var progressBar: CustomProgressBar? = null

    private var refreshDelay: Boolean = false

    lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        parentView = activity as MainView

        var pressedTime: Long = 0

        callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if(System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()

                    Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000)
                        .apply {
                            animationMode = Snackbar.ANIMATION_MODE_SLIDE
                        }
                        .show()

                    return
                }

                ActivityCompat.finishAffinity(parentView)
                exitProcess(0)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback ?: return)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        parentView = activity as MainView
        bindView(inflater, container)
        setAdapters()
        setObserver()


        //        binding.grid.setOnClickListener(null)

        /*  binding.tvApr.setOnClickListener {
              Toast.makeText(requireContext(), "4월", Toast.LENGTH_LONG).show()
              SopoLog.d("4월!!!!")
          }*/

        /*       binding.nestScrollViewComplete.setOnTouchListener(null)
               binding.nestScrollViewComplete.setOnClickListener(null)
               binding.nestScrollViewComplete.isClickable = false*/


        //        binding.vEmpty2.rootView.dispa
        binding.vEmpty2.setOnTouchListener { v, event ->
            return@setOnTouchListener binding.linearMonthSelector.dispatchTouchEvent(event)
        }



        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        setListener()
        binding.ivPopMenu.setOnClickListener {
            openInquiryMenu(it)
        }
    }

    override fun onDetach()
    {
        super.onDetach()

        callback.remove()
    }

    private fun bindView(inflater: LayoutInflater, container: ViewGroup?)
    {
        SopoLog.d("bindView() call")

        binding = DataBindingUtil.inflate<FragmentInquiryReBinding>(inflater,
                                                                    R.layout.fragment_inquiry_re,
                                                                    container, false).apply {
            vm = (this@InquiryFragment).vm
            lifecycleOwner = this@InquiryFragment
        }
    }

    private fun getAdapter(cntOfSelectedForDelete: MutableLiveData<Int>, inquiryItemTypeEnum: InquiryItemTypeEnum): InquiryListAdapter
    {
        return InquiryListAdapter(cntOfSelectedItemForDelete = cntOfSelectedForDelete,
                                  itemTypeEnum = inquiryItemTypeEnum).apply {
            this.setOnParcelClickListener(getParcelClicked())
        }
    }

    private fun setAdapters()
    {
        getAdapter(vm.cntOfSelectedItemForDelete, InquiryItemTypeEnum.Soon).let { adapter ->
            soonArrivalParcelAdapter = adapter
            binding.recyclerviewSoonArrival.adapter = soonArrivalParcelAdapter
        }
        getAdapter(vm.cntOfSelectedItemForDelete, InquiryItemTypeEnum.Registered).let { adapter ->
            registeredParcelAdapter = adapter
            binding.recyclerviewRegisteredParcel.adapter = registeredParcelAdapter
        }
        getAdapter(vm.cntOfSelectedItemForDelete, InquiryItemTypeEnum.Complete).let { adapter ->
            completedParcelAdapter = adapter
            binding.recyclerviewCompleteParcel.adapter = completedParcelAdapter
        }
    }

    private fun setObserver()
    {
        parentView.currentPage.observe(requireActivity(), Observer {
            if(it != null && it == TabCode.secondTab)
            {
                requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
            }
        })

        vm.isProgress.observe(requireActivity(), Observer { isProgress ->
            if(isProgress == null) return@Observer

            if(progressBar == null)
            {
                progressBar = CustomProgressBar(parentView)
            }

            progressBar?.onStartProgress(isProgress) { isDismiss ->
                if(isDismiss) progressBar = null
            }

        })

        // 배송중 , 등록된 택배 리스트
        vm.ongoingList.observe(requireActivity(), Observer { list ->

            soonArrivalParcelAdapter.separateDeliveryListByStatus(list)
            registeredParcelAdapter.separateDeliveryListByStatus(list)

            viewSettingForSoonArrivalList(soonArrivalParcelAdapter.getListSize())
            viewSettingForRegisteredList(registeredParcelAdapter.getListSize())
        })


        // '배송 중' 또는 '배송 완료' 화면에 따른 화면 세팅
        // TODO 데이터 바인딩으로 처리할 수 있으면 처리하도록 수정해야함.
        vm.screenStatusEnum.observe(requireActivity(), Observer {
            when(it)
            {
                ScreenStatusEnum.ONGOING ->
                { // '배송 중' 화면
                    binding.btnOngoing.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.MAIN_WHITE))
                    binding.btnOngoing.background = ContextCompat.getDrawable(requireContext(),
                                                                              R.drawable.border_all_rounded_light_black)
                    binding.btnOngoing.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.spoqa_han_sans_neo_regular)
                    binding.btnComplete.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.spoqa_han_sans_neo_regular)
                    binding.btnComplete.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
                    binding.btnComplete.background = ContextCompat.getDrawable(requireContext(),
                                                                               R.drawable.border_all_rounded_color_gray_400)
                }

                ScreenStatusEnum.COMPLETE ->
                { // '배송 완료' 화면
                    binding.btnOngoing.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
                    binding.btnOngoing.background = ContextCompat.getDrawable(requireContext(),
                                                                              R.drawable.border_all_rounded_color_gray_400)
                    binding.btnComplete.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.spoqa_han_sans_neo_regular)
                    binding.btnComplete.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.spoqa_han_sans_neo_regular)
                    binding.btnComplete.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.MAIN_WHITE))
                    binding.btnComplete.background = ContextCompat.getDrawable(requireContext(),
                                                                               R.drawable.border_all_rounded_light_black)
                }
            }
        })

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

        // '삭제하기'에서 선택된 아이템의 개수
        /*vm.cntOfSelectedItemForDelete.observe(requireActivity(), Observer {

            // 선택된 아이템이 1개 이상이라면 '~개 삭제 하기' 뷰가 나와야한다.
            if(it > 0)
            {
                binding.snackBarConfirmDelete.visibility = VISIBLE
            }
            // X 버튼을 눌러 삭제하기가 취소됐을때 '~개 삭제 하기' 뷰가 사라져야한다.
            else if(it == 0)
            {
                binding.snackBarConfirmDelete.visibility = GONE
            }

            // 아이템이 전부 선택되었는지를 확인(아이템이 존재하지 않을때 역시 '전체선택'으로 판단될 수 있으므로 선택된 아이템의 개수가 0 이상이어야한다.)
            if(vm.isFullySelected(it) && it != 0)
            {
                //'전채선택'이 됐다면 상단의 '전체선택 뷰'들의 이미지와 택스트를 빨간색으로 세팅한다.
                binding.imageIsAllChecked.setBackgroundResource(R.drawable.ic_checked_red)
                binding.tvIsAllChecked.setTextColor(
                    ContextCompat.getColor(requireActivity(), R.color.MAIN_RED))
            }
            else
            {
                //'전채선택'이 아니라면 상단의 '전체선택 뷰'들의 이미지와 택스트를 회색으로 세팅한다.
                binding.imageIsAllChecked.setBackgroundResource(R.drawable.ic_checked_gray)
                binding.tvIsAllChecked.setTextColor(
                    ContextCompat.getColor(requireActivity(), R.color.COLOR_GRAY_400))
            }
        })*/

        /*
         *   <화면에 리스트뷰들을 삭제할 수 있어야한다.>
         *   하나의 리스트뷰 아이템을 선택했을때 2가지의 경우의 수가 존재할 수 있다.
         *      1. 아이템들을 '삭제'할 수 있게 '선택' 되어야한다.
         *      2. 아이템들의 '상세 내역' 화면으로 '이동' 되어야한다.
         *   이 경우 viewModel의 liveData를 이용하여 아이템들을 '삭제'할 수 있는 상태라고 뷰들에게 알려준다.
         */
        vm.isRemovable.observe(requireActivity(), Observer {
            if(it)
            {
                //'삭제하기'일때
                // 리스트들에게 앞으로의 아이템들의 '클릭' 또는 '터치' 행위는 삭제하기 위한 '선택'됨을 뜻한다고 알려준다.
                soonArrivalParcelAdapter.setRemovable(true)
                registeredParcelAdapter.setRemovable(true)
                completedParcelAdapter.setRemovable(true)

                // 팝업 메뉴에서 '삭제하기'를 선택했을때의 화면 세팅
                viewSettingForPopupMenuDelete()
            }
            else
            {
                // '삭제하기 취소'일때
                // 선택되었지만 '취소'되어 '선택된 상태'를 다시 '미선택 상태'로 초기화한다.
                soonArrivalParcelAdapter.setRemovable(false)
                registeredParcelAdapter.setRemovable(false)
                completedParcelAdapter.setRemovable(false)

                // 삭제하기가 '취소' 되었을때 화면 세팅
                viewSettingForPopupMenuDeleteCancel()
            }
        })

        /*
         * 뷰모델에서 데이터 바인딩으로 '전체선택'하기를 이용자가 선택했을때
         */
        vm.isSelectAll.observe(requireActivity(), Observer {
            when(vm.screenStatusEnum.value)
            {
                ScreenStatusEnum.ONGOING ->
                {
                    soonArrivalParcelAdapter.setSelectAll(it)
                    registeredParcelAdapter.setSelectAll(it)
                }
                ScreenStatusEnum.COMPLETE ->
                {
                    completedParcelAdapter.setSelectAll(it)
                }
            }
        })

        /** 배송 완료
         */
        // 배송완료 리스트에서 해당 년월에 속해있는 택배들을 전부 삭제했을 때는 서버로 통신해서 새로고침하면 안돼고 무조건 로컬에 있는 데이터로 새로고침 해야한다.
        // (서버로 통신해서 새로고침하면 서버에 있는 데이터(우선순위가 높음)로 덮어써버리기 때문에 '삭제취소'를 통해 복구를 못함..)
        // (새로고침하면 내부에 저장된 '삭제할 데이터'들을 모두 서버로 통신하여 Remote database에서도 삭제처리(데이터 동기화)를 하고 나서 새로운 데이터를 받아옴)
        vm.refreshCompleteListByOnlyLocalData.observe(requireActivity(), Observer {
            if(it > 0)
            {
                vm.refreshCompleteListByOnlyLocalData()
            }
        })

        // 배송완료 리스트.
        vm.completeList.observe(requireActivity(), Observer { list ->
            SopoLog.d("!!! 완료 택배 갯수 ${list.size}")
            list.sortByDescending { it.parcelDTO.arrivalDte }

            val dumps = list.toMutableList()

            dumps.addAll(list)
            dumps.addAll(list)
            dumps.addAll(list)
            dumps.addAll(list)
            dumps.addAll(list)
            dumps.addAll(list)
            dumps.addAll(list)

            SopoLog.d("!!! 완료 택배 갯수 ${dumps.size}")

            completedParcelAdapter.notifyChanged(list)
        })

        // 현재 배송완료의 년월 데이터를 tv_spinner_month에 text 적용
        //        vm.currentParcelCntInfo.observe(requireActivity(), Observer {
        //            it?.let { entity ->
        //                binding.tvSpinnerMonth.text = MenuMapper.timeToListTitle(entity.time)
        //            }
        //        })

        // 배송완료 화면에서 표출 가능한 년월 리스트
        vm.completeDateList.observe(requireActivity()) { dates ->

            binding.constraintYearSpinner.setOnClickListener { v ->
                openPopUpMonthlyUsageHistory(v, dates)
            }
        }

        vm.currentCompleteParcelYear.observe(requireActivity()) {
            binding.tvSpinnerYear.text = "${it}년"
        }

        vm.currentCompleteParcelMonth.observe(requireActivity()) { list ->

            val reversedList = list.reversed()

            reversedList.forEach {

                val (clickable, textColor, font) = if(it.count > 0 && it.visibility == 1)
                {
                    Triple(true, ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_800),
                           ResourcesCompat.getFont(requireContext(),
                                                   R.font.spoqa_han_sans_neo_bold))
                }
                else
                {
                    Triple(false, ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_300),
                           ResourcesCompat.getFont(requireContext(),
                                                   R.font.spoqa_han_sans_neo_regular))
                }

                when(it.month)
                {
                    "01" ->
                    {
                        binding.tvJan.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "02" ->
                    {
                        binding.tvFeb.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "03" ->
                    {
                        binding.tvMar.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "04" ->
                    {
                        binding.tvApr.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "05" ->
                    {
                        binding.tvMay.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "06" ->
                    {
                        binding.tvJun.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "07" ->
                    {
                        binding.tvJul.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "08" ->
                    {
                        binding.tvAug.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "09" ->
                    {
                        binding.tvSep.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "10" ->
                    {
                        binding.tvOct.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "11" ->
                    {
                        binding.tvNov.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                    "12" ->
                    {
                        binding.tvDec.apply {
                            setTextColor(textColor)
                            typeface = font
                            //                            isClickable = clickable
                        }
                    }
                }

            }

        }
    }

    private fun getParcelClicked(): OnParcelClickListener
    {
        return object: OnParcelClickListener
        {
            override fun onItemClicked(view: View, type: Int, parcelId: Int)
            {
                TabCode.INQUIRY_DETAIL.FRAGMENT = ParcelDetailView.newInstance(parcelId)
                FragmentManager.move(requireActivity(), TabCode.INQUIRY_DETAIL,
                                     InquiryMainFrame.viewId)
            }

            override fun onItemLongClicked(view: View, type: Int, parcelId: Int)
            {
                val edit = MutableLiveData<String>()

                AlertUtil.updateValueDialog(requireContext(), "물품명을 입력해주세요.",
                                            Pair("확인", View.OnClickListener {
                                                edit.observe(requireActivity(), Observer { alias ->

                                                    val updateAliasRequest =
                                                        UpdateAliasRequest(parcelId = parcelId,
                                                                           alias = alias)

                                                    vm.patchParcelAlias(updateAliasRequest)

                                                    AlertUtil.onDismiss()

                                                    if(type == 0)
                                                    {
                                                        vm.refreshOngoingParcels()
                                                    }
                                                    else
                                                    {
                                                        vm.refreshComplete()
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
                                                                         vm.onOpenDeleteView()
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
    private fun openPopUpMonthlyUsageHistory(anchorView: View, statusList: MutableList<CompleteParcelStatusDTO>)
    {
        val historyPopUpView: PopupMenuViewBinding =
            PopupMenuViewBinding.inflate(LayoutInflater.from(context)).also { v ->

                    val inquiryMenuItems = MenuMapper.completeParcelStatusDTOToMenuItem(statusList) as MutableList<InquiryMenuItem>

                    val popupMenuListAdapter = PopupMenuListAdapter(inquiryMenuItems)

                    v.recyclerviewInquiryPopupMenu.also {
                        it.adapter = popupMenuListAdapter
                        val dividerItemDecoration =
                            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                        dividerItemDecoration.setDrawable(
                            ContextCompat.getDrawable(requireContext(), R.drawable.line_divider)!!)
                        it.addItemDecoration(dividerItemDecoration)

                        popupMenuListAdapter.setHistoryPopUpItemOnclick(object: PopupMenuListAdapter.HistoryPopUpItemOnclick
                                                                        {
                                                                            override fun changeTimeCount(v: View, year: String)
                                                                            {
                                                                                vm.changeYearForCompleteParcels(
                                                                                    year)
                                                                                historyPopUpWindow?.dismiss()
                                                                            }
                                                                        })

                        it.scrollBarFadeDuration = 800
                    }
                }
        historyPopUpWindow = if(statusList.size > 6)
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
                    ScreenStatusEnum.COMPLETE ->
                    {
                        vm.refreshComplete()
                    }
                    ScreenStatusEnum.ONGOING ->
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
        binding.recyclerviewCompleteParcel.addOnScrollListener(object: RecyclerView.OnScrollListener()
                                                               {
                                                                   override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
                                                                   {
                                                                       super.onScrollStateChanged(
                                                                           recyclerView, newState)

                                                                       if(!recyclerView.canScrollVertically(
                                                                               1) && newState == RecyclerView.SCROLL_STATE_IDLE
                                                                       ) // 리스트뷰의 마지막
                                                                       {
                                                                           val year =
                                                                               binding.tvSpinnerYear.text.toString()
                                                                                   .replace("년", "")
                                                                           SopoLog.d(
                                                                               msg = "[배송완료 - 다른 날짜의 데이터 조회] 선택된 년월 : $year")

                                                                           val month = "08"

                                                                           vm.getCompleteListWithPaging(
                                                                               MenuMapper.titleToInquiryDate(
                                                                                   "${year}${month}"))
                                                                       }
                                                                   }
                                                               })

        // '삭제하기' 화면에서 최하단에 '~개 삭제하기' 화면을 눌렀을때 실질적으로 '삭제' 행위를 개시한다.
        /*binding.snackBarConfirmDelete.setOnClickListener {
            ConfirmDeleteDialog(requireActivity(),
                                Pair("삭제하기", object: ((ConfirmDeleteDialog) -> Unit)
                                {
                                    override fun invoke(dialog: ConfirmDeleteDialog)
                                    {
                                        val selectedData = when(vm.getCurrentScreenStatus())
                                        {
                                            ScreenStatusEnum.ONGOING ->
                                            {
                                                val selectedDataSoon =
                                                    soonArrivalParcelAdapter.getSelectedListData() // '곧 도착'에서 선택된 아이템들 리스트
                                                val selectedDataRegister =
                                                    registeredParcelAdapter.getSelectedListData() // '등록된 택배'에서 선택된 아이템들 리스트
                                                Stream.of(selectedDataSoon, selectedDataRegister)
                                                    .flatMap { it.stream() }
                                                    .collect(
                                                        Collectors.toList()) // '곧 도착' 리스트와 '등록뙨 택배' 리스트에서 선택된 아이템들을 '하나'의 리스트로 합쳐 뷰모델로 보내 삭제 처리를 한다.
                                            }
                                            ScreenStatusEnum.COMPLETE ->
                                            {
                                                completedParcelAdapter.getSelectedListData()
                                            }
                                            else -> mutableListOf()
                                        }

                                        // 선택된 데이터들을 삭제한다.
                                        vm.removeSelectedData(selectedData as MutableList<Int>)
                                        // 삭제할 데이터의 크기를 알려준다.
                                        vm.setCntOfDelete(selectedData.size)

                                        // 삭제하기 화면을 종료한다.
                                        vm.closeRemoveView()
                                        dialog.dismiss()

                                        showDeleteSnackBar()
                                    }

                                })).show(requireActivity().supportFragmentManager,
                                         "ConfirmDeleteDialog")
        }*/
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
        /*
                binding.constraintDeleteSelect.visibility = VISIBLE

                binding.ivDelectStatusClear.visibility = VISIBLE

                binding.tvDeleteTitle.visibility = VISIBLE
        */

        // '하단 탭'이 사라져야한다.
        parentView.binding.vm!!.setMainTabVisibility(GONE)
        //        mainVm.setTabLayoutVisibility(GONE)
    }

    // X 버튼으로 '삭제하기 취소'가 되었을때 화면 세팅
    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    private fun viewSettingForPopupMenuDeleteCancel()
    {
        binding.tvTitle.visibility = VISIBLE
        binding.linearStatusSelector.visibility = VISIBLE
        binding.ivPopMenu.visibility = VISIBLE

        /*
                binding.ivDelectStatusClear.visibility = INVISIBLE
                binding.tvDeleteTitle.visibility = GONE
                binding.constraintDeleteSelect.visibility = GONE
        */

        // '하단 탭'이 노출되어야한다.
        parentView.binding.vm!!.setMainTabVisibility(VISIBLE)

        // 삭제하기 취소가 되었을때 화면의 리스트들을 앱이 켜졌을때 처럼 초기화 시켜준다.( '더보기'가 눌렸었는지 아니면 내가 전에 리스트들의 스크롤을 얼마나 내렸는지를 일일이 알고 있기 힘들기 때문에)
        viewSettingForSoonArrivalList(soonArrivalParcelAdapter.getListSize())
        viewSettingForRegisteredList(registeredParcelAdapter.getListSize())
    }

    /*    private fun showDeleteSnackBar()
        {
            val slideUp: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
            val slideDown: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)

            binding.snackBarDisplayDelete.visibility = VISIBLE
            binding.snackBarDisplayDelete.startAnimation(slideUp)
            //2초후에 실행
            Timer().schedule(object: TimerTask()
                             {
                                 override fun run()
                                 {
                                     CoroutineScope(Dispatchers.Main).launch {
                                         // 만약 '삭제취소'를 눌러서 삭제가 취소 되었을 경우, 이미 GONE이 된 상태이므로 애니메이션 효과를 줄 필요가 없다.
                                         if(binding.snackBarDisplayDelete.visibility != GONE)
                                         {
                                             binding.snackBarDisplayDelete.visibility = GONE
                                             binding.snackBarDisplayDelete.startAnimation(slideDown)
                                         }
                                     }
                                 }
                             }, 2000)
        }*/

    // 다른 화면으로 넘어갔을 때
    override fun onResume()
    {
        super.onResume()

        SopoLog.d(msg = "onResume")
    }
}