package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.databinding.SopoInquiryViewBinding
import com.delivery.sopo.enums.InquiryItemType
import com.delivery.sopo.enums.ScreenStatus
import com.delivery.sopo.mapper.MenuMapper
import com.delivery.sopo.models.entity.TimeCountEntity
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.repository.ParcelManagementRepoImpl
import com.delivery.sopo.repository.ParcelRepoImpl
import com.delivery.sopo.repository.TimeCountRepoImpl
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.util.fun_util.SizeUtil
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.MainViewModel
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.views.InquiryViewModelFactory
import com.delivery.sopo.views.MainViewModelFactory
import com.delivery.sopo.views.dialog.ConfirmDeleteDialog
import kotlinx.android.synthetic.main.sopo_inquiry_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.IllegalStateException
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

class InquiryView: Fragment() {

    private val TAG = this.javaClass.simpleName
    private val userRepo: UserRepo by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()
    private val parcelManagementRepoImpl: ParcelManagementRepoImpl by inject()
    private val timeCountRepoImpl: TimeCountRepoImpl by inject()
    private lateinit var binding: SopoInquiryViewBinding
    private lateinit var soonArrivalListAdapter: InquiryListAdapter
    private lateinit var registeredSopoListAdapter: InquiryListAdapter
    private lateinit var completeListAdapter: InquiryListAdapter
    private val mainVm: MainViewModel by lazy {
        ViewModelProvider(requireActivity(), MainViewModelFactory(userRepo)).get(MainViewModel::class.java)
    }
    private val inquiryVm: InquiryViewModel by lazy {
        ViewModelProvider(requireActivity(), InquiryViewModelFactory(userRepo, parcelRepoImpl, parcelManagementRepoImpl, timeCountRepoImpl)).get(InquiryViewModel::class.java)
    }
    private var progressBar: CustomProgressBar? = null
    private var refreshDelay: Boolean = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SopoInquiryViewBinding.inflate(inflater, container, false)
        progressBar = CustomProgressBar(requireActivity() as AppCompatActivity)
        viewBinding()
        setObserver()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewSetting()
        setListener()
        image_inquiry_popup_menu.setOnClickListener {
            openPopUpMenu(it)
        }
    }

    private fun viewBinding() {
        binding.vm = inquiryVm
        binding.lifecycleOwner = this

        soonArrivalListAdapter = InquiryListAdapter(inquiryVm.cntOfSelectedItem, this, mutableListOf(), InquiryItemType.Soon)
        binding.recyclerviewSoonArrival.adapter = soonArrivalListAdapter
        binding.recyclerviewSoonArrival.layoutManager = LinearLayoutManager(requireActivity())

        registeredSopoListAdapter = InquiryListAdapter(inquiryVm.cntOfSelectedItem, this, mutableListOf(), InquiryItemType.Registered)
        binding.recyclerviewRegisteredParcel.adapter = registeredSopoListAdapter
        binding.recyclerviewRegisteredParcel.layoutManager = LinearLayoutManager(requireActivity())

        completeListAdapter = InquiryListAdapter(inquiryVm.cntOfSelectedItem, this, mutableListOf(), InquiryItemType.Complete)
        binding.recyclerviewCompleteParcel.adapter = completeListAdapter
        binding.recyclerviewCompleteParcel.layoutManager = LinearLayoutManager(requireActivity())

        // 당겨서 새로고침 !
        binding.swipeRefresh.setOnRefreshListener {
            if(!refreshDelay){
                refreshDelay = true

                when(inquiryVm.getCurrentScreenStatus()){
                    ScreenStatus.COMPLETE -> {
                        inquiryVm.refreshComplete()
                    }
                    ScreenStatus.ONGOING -> {
                        inquiryVm.refreshOngoing()
                    }
                }

                //5초후에 실행
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        CoroutineScope(Dispatchers.Main).launch {
                            refreshDelay = false
                        }
                    }
                }, 5000)
            }
            else{
                Toast.makeText(requireContext(), "5초 후에 다시 새로고침을 시도해주세요.", Toast.LENGTH_LONG).show()
            }
            binding.swipeRefresh.isRefreshing = false
        }
        binding.executePendingBindings()
    }

    private fun setObserver(){

        // 배송완료 리스트에서 해당 년월에 속해있는 택배들을 전부 삭제했을 때는 서버로 통신해서 새로고침하면 안돼고 무조건 로컬에 있는 데이터로 새로고침 해야한다.
        // (서버로 통신해서 새로고침하면 서버에 있는 데이터(우선순위가 높음)로 덮어써버리기 때문에 '삭제취소'를 통해 복구를 못함..)
        // (새로고침하면 내부에 저장된 '삭제할 데이터'들을 모두 서버로 통신하여 Remote database에서도 삭제처리(데이터 동기화)를 하고 나서 새로운 데이터를 받아옴)
        inquiryVm.refreshCompleteListByOnlyLocalData.observe(this, Observer{
            if(it > 0){
                inquiryVm.refreshCompleteListByOnlyLocalData()
            }
        })

        inquiryVm.isLoading.observe(this, Observer { isLoading ->
            progressBar?.let {
                if (isLoading && !it.isAdded) {
                    it.onStartDialog()
                }
                else {
                    try {
                        it.onCloseDialog()
                    }
                    catch (e: IllegalStateException){
                        Log.e(TAG, "IllegalStateException !!!!!!! ${e.localizedMessage}")
                    }
                }
            }
        })

        // 배송중 , 등록된 택배 리스트
        inquiryVm.ongoingList.observe(this, Observer {
            soonArrivalListAdapter.setDataList(it)
            registeredSopoListAdapter.setDataList(it)

            viewSettingForSoonArrivalList(soonArrivalListAdapter.getListSize())
            viewSettingForRegisteredList(registeredSopoListAdapter.getListSize())
        })

        // 배송완료 리스트.
        inquiryVm.completeList.observe(this, Observer {list ->

            list.sortByDescending { it.parcel.arrivalDte }
            completeListAdapter.setDataList(list)
        })

        // 현재 배송완료의 년월 데이터를 tv_spinner_month에 text 적용
        inquiryVm.currentTimeCount.observe(this, Observer{
            it?.let {entity ->
                tv_spinner_month.text = MenuMapper.timeToListTitle(entity.time)
            }
        })

        // 배송완료 화면에서 표출 가능한 년월 리스트
        inquiryVm.monthList.observe(this, Observer { monthList ->
            constraint_month_spinner.setOnClickListener { v ->
                openPopUpMonthlyUsageHistory(v, monthList)
            }
        })

        // '배송 중' 또는 '배송 완료' 화면에 따른 화면 세팅
        // TODO 데이터 바인딩으로 처리할 수 있으면 처리하도록 수정해야함.
        inquiryVm.screenStatus.observe(this, Observer {
            when (it) {
                ScreenStatus.ONGOING -> { // '배송 중' 화면
                    btn_ongoing.setTextColor(ContextCompat.getColor(requireContext(), R.color.MAIN_WHITE))
                    btn_ongoing.background = ContextCompat.getDrawable(requireContext(), R.drawable.border_all_rounded_light_black)
                    btn_ongoing.typeface = ResourcesCompat.getFont(requireContext(), R.font.spoqa_han_sans_bold)
                    btn_complete.typeface = ResourcesCompat.getFont(requireContext(), R.font.spoqa_han_sans_regular)
                    btn_complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
                    btn_complete.background = ContextCompat.getDrawable(requireContext(), R.drawable.border_all_rounded_color_gray_400) }

                ScreenStatus.COMPLETE -> { // '배송 완료' 화면
                    btn_ongoing.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_400))
                    btn_ongoing.background = ContextCompat.getDrawable(requireContext(), R.drawable.border_all_rounded_color_gray_400)
                    btn_complete.typeface = ResourcesCompat.getFont(requireContext(), R.font.spoqa_han_sans_regular)
                    btn_complete.typeface = ResourcesCompat.getFont(requireContext(), R.font.spoqa_han_sans_bold)
                    btn_complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.MAIN_WHITE))
                    btn_complete.background = ContextCompat.getDrawable(requireContext(), R.drawable.border_all_rounded_light_black)
                }
            }
        })

         // '더 보기'로 아이템들을 숨기는 것을 해제하여 모든 아이템들을 화면에 노출시킨다.
        // TODO 데이터 바인딩으로 처리할 수 있으면 처리하도록 수정해야함.
        inquiryVm.isMoreView.observe(this, Observer {
            if (it) {
                // '곧 도착' 리스트뷰는 2개 이상의 데이터는 '더 보기'로 숨겨져 있기 때문에 어덥터에 모든 데이터를 표출하라고 지시한다.
                soonArrivalListAdapter.isFullListItem(true)

                // 모든 아이템들을 노출 시켰을때 화면 세팅
                linear_more_view.visibility = VISIBLE
                tv_more_view.text = ""
                image_arrow.setBackgroundResource(R.drawable.ic_up_arrow)
            }
            else {
                // '곧 도착' 리스트뷰의 2개 이상의 데이터가 존재할떄 '더 보기'로 숨기라고 지시한다.
                soonArrivalListAdapter.isFullListItem(false)

                // 제한된 아이템들을 노출 시킬때의 화면 세팅
                linear_more_view.visibility = VISIBLE
                tv_more_view.text = "더 보기"
                image_arrow.setBackgroundResource(R.drawable.ic_down_arrow)
            }
        })

         // '삭제하기'에서 선택된 아이템의 개수
        inquiryVm.cntOfSelectedItem.observe(this, Observer {

            // 선택된 아이템이 1개 이상이라면 '~개 삭제 하기' 뷰가 나와야한다.
            if (it > 0) {
                constraint_delete_final.visibility = VISIBLE
            }
            // X 버튼을 눌러 삭제하기가 취소됐을때 '~개 삭제 하기' 뷰가 사라져야한다.
            else if (it == 0) {
                constraint_delete_final.visibility = GONE
            }

            // 아이템이 전부 선택되었는지를 확인(아이템이 존재하지 않을때 역시 '전체선택'으로 판단될 수 있으므로 선택된 아이템의 개수가 0 이상이어야한다.)
            if (inquiryVm.isFullySelected(it) && it != 0) {
                //'전채선택'이 됐다면 상단의 '전체선택 뷰'들의 이미지와 택스트를 빨간색으로 세팅한다.
                image_is_all_checked.setBackgroundResource(R.drawable.ic_checked_red)
                tv_is_all_checked.setTextColor(ContextCompat.getColor(requireActivity(), R.color.MAIN_RED))
            }
            else {
                //'전채선택'이 아니라면 상단의 '전체선택 뷰'들의 이미지와 택스트를 회색으로 세팅한다.
                image_is_all_checked.setBackgroundResource(R.drawable.ic_checked_gray)
                tv_is_all_checked.setTextColor(ContextCompat.getColor(requireActivity(), R.color.COLOR_GRAY_400))
            }
        })

        /*
         *   <화면에 리스트뷰들을 삭제할 수 있어야한다.>
         *   하나의 리스트뷰 아이템을 선택했을때 2가지의 경우의 수가 존재할 수 있다.
         *      1. 아이템들을 '삭제'할 수 있게 '선택' 되어야한다.
         *      2. 아이템들의 '상세 내역' 화면으로 '이동' 되어야한다.
         *   이 경우 viewModel의 liveData를 이용하여 아이템들을 '삭제'할 수 있는 상태라고 뷰들에게 알려준다.
         */
        inquiryVm.isRemovable.observe(this, Observer {
            if (it) {
                //'삭제하기'일때
                // 리스트들에게 앞으로의 아이템들의 '클릭' 또는 '터치' 행위는 삭제하기 위한 '선택'됨을 뜻한다고 알려준다.
                soonArrivalListAdapter.setRemovable(true)
                registeredSopoListAdapter.setRemovable(true)
                completeListAdapter.setRemovable(true)

                // 팝업 메뉴에서 '삭제하기'를 선택했을때의 화면 세팅
                viewSettingForPopupMenuDelete()
            }
            else {
                // '삭제하기 취소'일때
                // 선택되었지만 '취소'되어 '선택된 상태'를 다시 '미선택 상태'로 초기화한다.
                soonArrivalListAdapter.setRemovable(false)
                registeredSopoListAdapter.setRemovable(false)
                completeListAdapter.setRemovable(false)

                // 삭제하기가 '취소' 되었을때 화면 세팅
                viewSettingForPopupMenuDeleteCancel()
            }
        })

        /*
         * 뷰모델에서 데이터 바인딩으로 '전체선택'하기를 이용자가 선택했을때
         */
        inquiryVm.isSelectAll.observe(this, Observer {
            when (inquiryVm.screenStatus.value)
            {
                ScreenStatus.ONGOING ->
                {
                    soonArrivalListAdapter.setSelectAll(it)
                    registeredSopoListAdapter.setSelectAll(it)
                }
                ScreenStatus.COMPLETE ->
                {
                    completeListAdapter.setSelectAll(it)
                }
            }
        })
    }

    // 메뉴를 눌렀을때 팝업 메뉴를 띄운다.
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun openPopUpMenu(anchorView: View){

        val listPopupWindow = ListPopupWindow(requireActivity()).apply {
            this.width = SizeUtil.changeDpToPx(binding.root.context, 150F)
            this.setBackgroundDrawable(requireActivity().getDrawable(R.drawable.border_all_rounded_no_storke))
        }

        listPopupWindow.anchorView = anchorView
        val menu = PopupMenu(requireActivity(), anchorView).menu
        // 화면 inflate
        requireActivity().menuInflater.inflate(R.menu.inquiry_popup_menu, menu)

        // 팝업 메뉴 세팅
        val listPopupWindowAdapter = InquiryListPopupWindowAdapter(
            requireActivity(), MenuMapper.menuToMenuItemList(
                menu
            )
        )
        listPopupWindow.setAdapter(listPopupWindowAdapter)
        listPopupWindow.setOnItemClickListener{ parent, view, position, id ->
            when(position){
                //삭제하기
                0 -> {
                    inquiryVm.openRemoveView()
                }
                // 새로고침
                1 -> {
                    inquiryVm.refreshOngoing()
                }
                // 도움말
                2 -> {
                }
            }
            listPopupWindow.dismiss()
        }
        listPopupWindow.show()
    }

    // 배송완료 화면에서 년/월을 눌렀을 시 팝업 메뉴가 나온다.
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun openPopUpMonthlyUsageHistory(
        anchorView: View,
        timeCntDtoList: MutableList<TimeCountEntity>
    ){

        val listPopupWindow = ListPopupWindow(requireActivity()).apply {
            this.width = SizeUtil.changeDpToPx(binding.root.context, 120F)
            this.setBackgroundDrawable(requireActivity().getDrawable(R.drawable.border_all_rounded_no_storke))
        }

        listPopupWindow.anchorView = anchorView
        val menu = PopupMenu(requireActivity(), anchorView).menu
        // 화면 inflate
        requireActivity().menuInflater.inflate(R.menu.basic_menu, menu)

        // 팝업 메뉴 세팅
        val listPopupWindowAdapter = InquiryListPopupWindowAdapter(
            requireActivity(), MenuMapper.timeCountDtoToMenuItemList(timeCntDtoList)
        )
        listPopupWindow.setAdapter(listPopupWindowAdapter)

        // 6개 이상이라면 6개까지만 크기를 늘리고 그 이상의 데이터는 스크롤로 내리도록함
        if(timeCntDtoList.size > 6){
            val measureContentHeight = SizeUtil.measureContentHeight(
                requireContext(),
                listPopupWindowAdapter
            )
            Log.d(TAG, "measureContentHeight : $measureContentHeight")
            listPopupWindow.height = SizeUtil.measureContentHeight(
                requireContext(),
                listPopupWindowAdapter
            ) * 6
        }
        else{
            listPopupWindow.height = ListPopupWindow.WRAP_CONTENT
        }

        // 해당 년월을 눌렀을때 페이징해서 서버에서 데이터를 가져오고 텍스트뷰를 선택한 년월로 세팅한다.
        listPopupWindow.setOnItemClickListener{ parent, view, position, id ->
            listPopupWindowAdapter.getItem(position).timeCount?.let {
                if(it.count > 0){
                    inquiryVm.changeTimeCount(it.time)
                    listPopupWindow.dismiss()
                }
            }
        }
        listPopupWindow.show()
    }

    private fun setListener(){
        // 배송완료 리스트의 마지막 행까지 내려갔다면 다음 데이터를 요청한다(페이징)
        binding.recyclerviewCompleteParcel.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
            {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) // 리스트뷰의 마지막
                {
                    val spinnerMonthTv = tv_spinner_month.text.toString()
                    Log.d(TAG, "[배송완료 - 다른 날짜의 데이터 조회] 선택된 년월 : $spinnerMonthTv")
                    inquiryVm.getCompleteListWithPaging(MenuMapper.titleToInquiryDate(tv_spinner_month.text.toString()))
                }
            }
        })

        // '삭제하기' 화면에서 최하단에 '~개 삭제하기' 화면을 눌렀을때 실질적으로 '삭제' 행위를 개시한다.
        constraint_delete_final.setOnClickListener {
            ConfirmDeleteDialog(requireActivity()){ dialog ->

                val selectedData
                        = when(inquiryVm.getCurrentScreenStatus()){
                                ScreenStatus.ONGOING -> {
                                    val selectedDataSoon = soonArrivalListAdapter.getSelectedListData() // '곧 도착'에서 선택된 아이템들 리스트
                                    val selectedDataRegister = registeredSopoListAdapter.getSelectedListData() // '등록된 택배'에서 선택된 아이템들 리스트
                                    Stream.of(selectedDataSoon, selectedDataRegister).flatMap { it.stream()}.collect(Collectors.toList()) // '곧 도착' 리스트와 '등록뙨 택배' 리스트에서 선택된 아이템들을 '하나'의 리스트로 합쳐 뷰모델로 보내 삭제 처리를 한다.
                                }
                                ScreenStatus.COMPLETE -> {
                                    completeListAdapter.getSelectedListData()
                                }
                                else -> mutableListOf()
                            }

                // 선택된 데이터들을 삭제한다.
                inquiryVm.removeSelectedData(selectedData as MutableList<ParcelId>)
                // 삭제할 데이터의 크기를 알려준다.
                inquiryVm.setCntOfDelete(selectedData.size)

                // 삭제하기 화면을 종료한다.
                inquiryVm.closeRemoveView()
                dialog.dismiss()

                showDeleteSnackBar()
            }
            .show(requireActivity().supportFragmentManager, "ConfirmDeleteDialog")
        }
    }

    // 초기화면 세팅
    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    private fun initViewSetting(){
        tv_title.visibility = VISIBLE
        constraint_soon_arrival.visibility = VISIBLE
        linear_more_view_parent.visibility = GONE
        v_more_view.visibility = GONE
        constraint_select.visibility = VISIBLE
        constraint_delete_select.visibility = GONE
        image_inquiry_popup_menu.visibility = VISIBLE
        image_inquiry_popup_menu_close.visibility = GONE
        constraint_delete_final.visibility = GONE
        tv_delete_title.visibility = GONE
        constraint_snack_bar.visibility = GONE
    }

    // '곧 도착' 리스트의 아이템의 개수에 따른 화면세팅
    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    private fun viewSettingForSoonArrivalList(listSize: Int){
        when(listSize){
            // 아이템의 개수가 0개일때
            0 ->
            {
                // '곧 도착' 텍스트부터 리스트뷰까지 잡혀있는 부모뷰를 GONE 처리
                constraint_soon_arrival.visibility = GONE
                // '더보기'를  선택할 수 있는 부모뷰 GONE 처리
                linear_more_view_parent.visibility = GONE
                // '곧 도착'과 '등록된 택배'의 사이에 적절한 공백을 담당하는 뷰 GONE 처리
                v_more_view.visibility = GONE
            }
            1 ->
            {
                constraint_soon_arrival.visibility = VISIBLE
                linear_more_view_parent.visibility = GONE
                v_more_view.visibility = INVISIBLE
            }
            2 ->
            {
                constraint_soon_arrival.visibility = VISIBLE
                linear_more_view_parent.visibility = GONE
                v_more_view.visibility = INVISIBLE
            }
            else -> {
                constraint_soon_arrival.visibility = VISIBLE
                linear_more_view_parent.visibility = VISIBLE
                v_more_view.visibility = GONE
            }
        }
    }

    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    private fun viewSettingForRegisteredList(listSize: Int){
        when(listSize){
            0 ->
            {
                constraint_registered_arrival.visibility = GONE
            }
            else -> {
                constraint_registered_arrival.visibility = VISIBLE
            }
        }
    }

    //팝업 메뉴에서 '삭제하기'가 선택되었을때 화면 세팅
    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    private fun viewSettingForPopupMenuDelete(){
        tv_title.visibility = INVISIBLE
        constraint_select.visibility = INVISIBLE
        image_inquiry_popup_menu.visibility = INVISIBLE
        image_inquiry_popup_menu_close.visibility = VISIBLE
        linear_more_view_parent.visibility = GONE
        v_more_view.visibility = INVISIBLE
        constraint_delete_select.visibility = VISIBLE
        tv_delete_title.visibility = VISIBLE

        // '하단 탭'이 사라져야한다.
        mainVm.setTabLayoutVisiblity(GONE)
    }

    // X 버튼으로 '삭제하기 취소'가 되었을때 화면 세팅
    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    private fun viewSettingForPopupMenuDeleteCancel(){
        tv_title.visibility = VISIBLE
        constraint_select.visibility = VISIBLE
        image_inquiry_popup_menu.visibility = VISIBLE
        image_inquiry_popup_menu_close.visibility = INVISIBLE
        tv_delete_title.visibility = GONE
        constraint_delete_select.visibility = GONE

        // '하단 탭'이 노출되어야한다.
        mainVm.setTabLayoutVisiblity(VISIBLE)

        // 삭제하기 취소가 되었을때 화면의 리스트들을 앱이 켜졌을때 처럼 초기화 시켜준다.( '더보기'가 눌렸었는지 아니면 내가 전에 리스트들의 스크롤을 얼마나 내렸는지를 일일이 알고 있기 힘들기 때문에)
        viewSettingForSoonArrivalList(soonArrivalListAdapter.getListSize())
        viewSettingForRegisteredList(registeredSopoListAdapter.getListSize())
    }

    private fun showDeleteSnackBar(){
        val slideUp: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        val slideDown: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)

        constraint_snack_bar.visibility = VISIBLE
        constraint_snack_bar.startAnimation(slideUp)
        //2초후에 실행
        Timer().schedule(object : TimerTask()
        {
            override fun run()
            {
                CoroutineScope(Dispatchers.Main).launch {
                    // 만약 '삭제취소'를 눌러서 삭제가 취소 되었을 경우, 이미 GONE이 된 상태이므로 애니메이션 효과를 줄 필요가 없다.
                    if(constraint_snack_bar.visibility != GONE){
                        constraint_snack_bar.visibility = GONE
                        constraint_snack_bar.startAnimation(slideDown)
                    }
                }
            }
        }, 2000)
    }
}