package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatus
import com.delivery.sopo.databinding.SopoInquiryViewBinding
import com.delivery.sopo.models.dto.Resource
import com.delivery.sopo.models.inquiry.InquiryListData
import com.delivery.sopo.viewmodels.MainViewModel
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.views.dialog.ConfirmDeleteDialog
import kotlinx.android.synthetic.main.sopo_inquiry_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.stream.Collectors
import java.util.stream.Stream


/*
    TODO : 로컬에서 Parcel의 status가 3인데 서버는 0인 경우 => 앱에서 지우라고 서버에 요청을해서 서버는 지웠지만 앱은 서버의 성공 요청을 못 받은 경우,
           로컬에서 다시 한번 삭제요청을 하여 로컬에 데이터가 동기화됐을때 '등록된 택배' TextView가 사라지지 않음(아이템 갯수가 0개인데도 불구하고)
 */

class InquiryView: Fragment() {

    private val TAG = this.javaClass.simpleName
    private val inquiryVM: InquiryViewModel by viewModel()
    private lateinit var binding: SopoInquiryViewBinding
    private lateinit var soonArrivalListAdapter: SoonArrivalListAdapter
    private lateinit var registeredSopoListAdapter: RegisteredSopoListAdapter
    private var soonArrivalList: MutableList<InquiryListData> = mutableListOf()
    private var registeredSopoList: MutableList<InquiryListData> = mutableListOf()

    private val mainVm : MainViewModel by viewModel()
//    private val mainVm: MainViewModel by lazy {
//                    ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
//                        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
//                            MainViewModel() as T
//                    }).get(MainViewModel::class.java)
//                }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SopoInquiryViewBinding.inflate(inflater, container, false)
        viewBinding()
        setObserver()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        initViewSetting()
        setListener()
        image_inquiry_popup_menu.setOnClickListener {
            showListPopupWindow(it)
        }
    }

    private fun viewBinding() {
        binding.vm = inquiryVM
        binding.lifecycleOwner = this

        soonArrivalListAdapter = SoonArrivalListAdapter(inquiryVM.cntOfSelectedItem, this, mutableListOf())
        binding.recyclerviewSoonArrival.adapter = soonArrivalListAdapter
        binding.recyclerviewSoonArrival.layoutManager = LinearLayoutManager(requireActivity())

        registeredSopoListAdapter = RegisteredSopoListAdapter(inquiryVM.cntOfSelectedItem, this, mutableListOf())
        binding.recyclerviewRegisteredParcel.adapter = registeredSopoListAdapter
        binding.recyclerviewRegisteredParcel.layoutManager = LinearLayoutManager(requireActivity())

        binding.executePendingBindings()
    }

    private fun setObserver(){

        inquiryVM.soonList.observe(this, Observer{
            // '곧 도착' 리스트의 아이템의 개수에 따른 화면 세팅
            viewSettingForSoonArrivalList(it.size)
            soonArrivalListAdapter.setDataList(it)
        })

        inquiryVM.registerList.observe(this, Observer{
            // '등록된 택배' 리스트의 아이템의 개수에 따른 화면 세팅
            viewSettingForRegisteredList(it.size)
            registeredSopoListAdapter.setDataList(it)
        })

        /*
         *  '더 보기'로 아이템들을 숨기는 것을 해제하여 모든 아이템들을 화면에 노출시킨다.
         */
        inquiryVM.isMoreView.observe(this, Observer{
            if(it){
                // '곧 도착' 리스트뷰는 2개 이상의 데이터는 '더 보기'로 숨겨져 있기 때문에 어덥터에 모든 데이터를 표출하라고 지시한다.
                soonArrivalListAdapter.isFullListItem(true)

                // 모든 아이템들을 노출 시켰을때 화면 세팅
                linear_more_view.visibility = View.VISIBLE
                tv_more_view.text = ""
                image_arrow.setBackgroundResource(R.drawable.ic_up_arrow)
            }
            else{
                // '곧 도착' 리스트뷰의 2개 이상의 데이터가 존재할떄 '더 보기'로 숨기라고 지시한다.
                soonArrivalListAdapter.isFullListItem(false)

                // 제한된 아이템들을 노출 시킬때의 화면 세팅
                linear_more_view.visibility = View.VISIBLE
                tv_more_view.text = "더 보기"
                image_arrow.setBackgroundResource(R.drawable.ic_down_arrow)
            }
        })

        /*
         *  '삭제하기'에서 선택된 아이템의 개수
         */
        inquiryVM.cntOfSelectedItem.observe(this, Observer{

            // 선택된 아이템이 1개 이상이라면 '~개 삭제 하기' 뷰가 나와야한다.
            if(it > 0){
                constraint_delete_final.visibility = View.VISIBLE
            }
            // X 버튼을 눌러 삭제하기가 취소됐을때 '~개 삭제 하기' 뷰가 사라져야한다.
            else if(it == 0){
                constraint_delete_final.visibility = View.GONE
            }

            // 아이템이 전부 선택되었는지를 확인(아이템이 존재하지 않을때 역시 '전체선택'으로 판단될 수 있으므로 선택된 아이템의 개수가 0 이상이어야한다.)
            if( inquiryVM.isFullySelected(it) && it != 0 )
            {
                //'전채선택'이 됐다면 상단의 '전체선택 뷰'들의 이미지와 택스트를 빨간색으로 세팅한다.
                image_is_all_checked.setBackgroundResource(R.drawable.ic_checked_red)
                tv_is_all_checked.setTextColor(ContextCompat.getColor(requireActivity(), R.color.MAIN_RED))
            }
            else{
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
        inquiryVM.isRemovable.observe(this, Observer {
            if(it){ //'삭제하기'일때
                // 리스트들에게 앞으로의 아이템들의 '클릭' 또는 '터치' 행위는 삭제하기 위한 '선택'됨을 뜻한다고 알려준다.
                soonArrivalListAdapter.setRemovable(true)
                registeredSopoListAdapter.setRemovable(true)

                // 팝업 메뉴에서 '삭제하기'를 선택했을때의 화면 세팅
                viewSettingforPopupMenuDelete()
            }
            else{ // '삭제하기 취소'일때
                // 선택되었지만 '취소'되어 '선택된 상태'를 다시 '미선택 상태'로 초기화한다.
                soonArrivalListAdapter.setRemovable(false)
                registeredSopoListAdapter.setRemovable(false)

                // 삭제하기가 '취소' 되었을때 화면 세팅
                viewSettingforPopupMenuDelete_Cancel()
            }
        })

        /*
         * 뷰모델에서 데이터 바인딩으로 '전체선택'하기를 이용자가 선택했을때
         */
        inquiryVM.isSelectAll.observe(this, Observer{
                soonArrivalListAdapter.setSelectAll(it)
                registeredSopoListAdapter.setSelectAll(it)
        })

    }

    // 메뉴를 눌렀을때 팝업 메뉴를 띄운다.
    private fun showListPopupWindow(anchorView: View){

        val listPopupWindow = ListPopupWindow(requireActivity()).apply {
            this.width = 600
            this.setBackgroundDrawable(requireActivity().getDrawable(R.drawable.border_all_rounded_no_storke))
        }

        listPopupWindow.anchorView = anchorView
        val menu = PopupMenu(requireActivity(), anchorView).menu
        // 화면 inflate
        requireActivity().menuInflater.inflate(R.menu.inquiry_popup_menu, menu)

        // 팝업 메뉴 세팅
        val listPopupWindowAdapter = InquiryListPopupWindowAdapter(requireActivity(), menu)
        listPopupWindow.setAdapter(listPopupWindowAdapter)
        listPopupWindow.setOnItemClickListener{
            parent, view, position, id ->
            when(position){
                //삭제하기
                0 -> {
                    inquiryVM.openRemoveView()
                }
                // 새로고침
                1 -> {
                }
                // 도움말
                2 -> {
                }
            }
            listPopupWindow.dismiss()
        }
        listPopupWindow.show()
    }

    private fun setListener(){

        // '삭제하기' 화면에서 최하단에 '~개 삭제하기' 화면을 눌렀을때 실질적으로 '삭제' 행위를 개시한다.
        constraint_delete_final.setOnClickListener {
            ConfirmDeleteDialog(requireActivity()){
                dialog ->

                // '곧 도착'에서 선택된 아이템들 리스트
                val selectedDataSoon = soonArrivalListAdapter.getSelectedListData()
                // '등록된 택배'에서 선택된 아이템들 리스트
                val selectedDataRegister = registeredSopoListAdapter.getSelectedListData()
                // '곧 도착' 리스트와 '등록뙨 택배' 리스트에서 선택된 아이템들을 '하나'의 리스트로 합쳐 뷰모델로 보내 삭제 처리를 한다.
                val selectedData = Stream.of(selectedDataSoon, selectedDataRegister).flatMap { it.stream()}.collect(Collectors.toList())

                // 선택된 데이터들을 삭제한다.
                inquiryVM.removeSelectedData(selectedData)

                // TODO: 로딩화면으로 데이터가 삭제되고 화면이 다시 그려지기 전까지의 화면을 대체한다.

                // 삭제하기 화면을 종료한다.
                inquiryVM.closeRemoveView()
                dialog.dismiss()
            }
                .show(requireActivity().supportFragmentManager, "ConfirmDeleteDialog")
        }
    }

    // 초기화면 세팅
    private fun initViewSetting(){
        tv_title.visibility = View.VISIBLE
        constraint_soon_arrival.visibility = View.VISIBLE
        linear_more_view_parent.visibility = View.GONE
        v_more_view.visibility = View.GONE
        constraint_select.visibility = View.VISIBLE
        constraint_delete_select.visibility = View.GONE
        image_inquiry_popup_menu.visibility = View.VISIBLE
        image_inquiry_popup_menu_close.visibility = View.GONE
        constraint_delete_final.visibility = View.GONE
        tv_delete_title.visibility = View.GONE
    }

    /*
     * '곧 도착' 리스트의 아이템의 개수에 따른 화면세팅
     */
    private fun viewSettingForSoonArrivalList(listSize: Int){
        when(listSize){
            // 아이템의 개수가 0개일때
            0 -> {
                // '곧 도착' 텍스트부터 리스트뷰까지 잡혀있는 부모뷰를 GONE 처리
                constraint_soon_arrival.visibility = View.GONE
                // '더보기'를  선택할 수 있는 부모뷰 GONE 처리
                linear_more_view_parent.visibility = View.GONE
                // '곧 도착'과 '등록된 택배'의 사이에 적절한 공백을 담당하는 뷰 GONE 처리
                v_more_view.visibility = View.GONE
            }
            1-> {
                constraint_soon_arrival.visibility = View.VISIBLE
                linear_more_view_parent.visibility = View.GONE
                v_more_view.visibility = View.INVISIBLE
            }
            2-> {
                constraint_soon_arrival.visibility = View.VISIBLE
                linear_more_view_parent.visibility = View.GONE
                v_more_view.visibility = View.INVISIBLE
            }
            else -> {
                constraint_soon_arrival.visibility = View.VISIBLE
                linear_more_view_parent.visibility = View.VISIBLE
                v_more_view.visibility = View.GONE
            }
        }
    }
    private fun viewSettingForRegisteredList(listSize: Int){
        //TODO : 작성해야함
    }

    //팝업 메뉴에서 '삭제하기'가 선택되었을때 화면 세팅
    private fun viewSettingforPopupMenuDelete(){
        tv_title.visibility = View.INVISIBLE
        constraint_select.visibility = View.INVISIBLE
        image_inquiry_popup_menu.visibility = View.INVISIBLE
        image_inquiry_popup_menu_close.visibility = View.VISIBLE
        linear_more_view_parent.visibility = View.GONE
        v_more_view.visibility = View.INVISIBLE
        constraint_delete_select.visibility = View.VISIBLE
        tv_delete_title.visibility = View.VISIBLE

        // '하단 탭'이 사라져야한다.
        mainVm.setTabLayoutVisiblity(View.GONE)
    }

    // X 버튼으로 '삭제하기 취소'가 되었을때 화면 세팅
    private fun viewSettingforPopupMenuDelete_Cancel(){
        tv_title.visibility = View.VISIBLE
        constraint_select.visibility = View.VISIBLE
        image_inquiry_popup_menu.visibility = View.VISIBLE
        image_inquiry_popup_menu_close.visibility = View.INVISIBLE
        tv_delete_title.visibility = View.GONE
        constraint_delete_select.visibility = View.GONE

        // '하단 탭'이 노출되어야한다.
        mainVm.setTabLayoutVisiblity(View.VISIBLE)

        // 삭제하기 취소가 되었을때 화면의 리스트들을 앱이 켜졌을때 처럼 초기화 시켜준다.( '더보기'가 눌렸었는지 아니면 내가 전에 리스트들의 스크롤을 얼마나 내렸는지를 일일이 알고 있기 힘들기 때문에)
        viewSettingForSoonArrivalList(soonArrivalListAdapter.getListSize())
        viewSettingForRegisteredList(registeredSopoListAdapter.getListSize())
    }
}