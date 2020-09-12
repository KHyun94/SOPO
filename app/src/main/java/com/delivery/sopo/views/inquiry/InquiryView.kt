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
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatus
import com.delivery.sopo.databinding.SopoInquiryViewBinding
import com.delivery.sopo.models.inquiry.InquiryListData
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.views.dialog.ConfirmDeleteDialog
import kotlinx.android.synthetic.main.sopo_inquiry_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.stream.Collectors
import java.util.stream.Stream

class InquiryView: Fragment() {

    private lateinit var binding: SopoInquiryViewBinding
    private val inquiryVM: InquiryViewModel by viewModel()
    private lateinit var soonArrivalListAdapter: SoonArrivalListAdapter
    private lateinit var registeredSopoListAdapter: RegisteredSopoListAdapter
    private var soonArrivalList: MutableList<InquiryListData> = mutableListOf()
    private var registeredSopoList: MutableList<InquiryListData> = mutableListOf()
    private val TAG = this.javaClass.simpleName
//    private val inquiryFragmentContext = requireActivity()

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
        inquiryVM.parcelList.observe(this, Observer {
            parcelList ->

            parcelList?.let{

                val filteredSoonArrivalList =parcelList.filter { parcel ->
                    // 리스트 중 오직 '배송출발'일 경우만 해당 adapter로 넘긴다.
                    parcel.deliveryStatus == DeliveryStatus.OUT_FOR_DELIVERY
                }.also {
                    // 화면 세팅
                    viewSettingForSoonArrivalList(it.size)
                }.map{
                    InquiryListData(parcel = it)
                } as MutableList<InquiryListData>
                soonArrivalList = filteredSoonArrivalList
                soonArrivalListAdapter.setDataList(filteredSoonArrivalList)


                val filteredRegisteredSopoList = parcelList.filter { parcel ->
                    // 리스트 중 오직 '배송출발'과 '배송도착'이 아닐 경우만 해당 adapter로 넘긴다.
                    parcel.deliveryStatus != DeliveryStatus.OUT_FOR_DELIVERY && parcel.deliveryStatus != DeliveryStatus.DELIVERED
                }.also {
                    // 화면 세팅
                    viewSettingForRegisteredList(it.size)
                }.map {
                    InquiryListData(parcel = it)
                } as MutableList<InquiryListData>
                registeredSopoList = filteredRegisteredSopoList
                registeredSopoListAdapter.setDataList(filteredRegisteredSopoList)
            }
        })

        inquiryVM.isMoreView.observe(this, Observer{
            if(it){
                soonArrivalListAdapter.isFullListItem(true)
                linear_more_view.visibility = View.VISIBLE
                tv_more_view.text = ""
                image_arrow.setBackgroundResource(R.drawable.ic_up_arrow)
            }
            else{
                soonArrivalListAdapter.isFullListItem(false)
                linear_more_view.visibility = View.VISIBLE
                tv_more_view.text = "더 보기"
                image_arrow.setBackgroundResource(R.drawable.ic_down_arrow)
            }
        })

        inquiryVM.cntOfSelectedItem.observe(this, Observer{
            if(it > 0){
                constraint_delete_final.visibility = View.VISIBLE
            }
            else if(it == 0){
                constraint_delete_final.visibility = View.GONE
            }

            if(it == (soonArrivalList.size + registeredSopoList.size) && it != 0){
                image_is_all_checked.setBackgroundResource(R.drawable.ic_checked_red)
                tv_is_all_checked.setTextColor(ContextCompat.getColor(requireActivity(), R.color.MAIN_RED))
            }
            else{
                image_is_all_checked.setBackgroundResource(R.drawable.ic_checked_gray)
                tv_is_all_checked.setTextColor(ContextCompat.getColor(requireActivity(), R.color.COLOR_GRAY_400))
            }
        })

        inquiryVM.isRemovable.observe(this, Observer {
            if(it){
                soonArrivalListAdapter.setRemovable(true)
                registeredSopoListAdapter.setRemovable(true)
                viewSettingforPopupMenuDelete()
            }
            else{
                soonArrivalListAdapter.setRemovable(false)
                soonArrivalListAdapter.cancelRemoveItem()
                registeredSopoListAdapter.setRemovable(false)
                registeredSopoListAdapter.cancelRemoveItem()

                inquiryVM.setMoreView(false)
                viewSettingforPopupMenuDelete_Cancel()
            }
        })

        inquiryVM.isSelectAll.observe(this, Observer{
                soonArrivalListAdapter.setSelectAll(it)
                registeredSopoListAdapter.setSelectAll(it)
        })

    }

    private fun showListPopupWindow(anchorView: View){

        val listPopupWindow = ListPopupWindow(requireActivity()).apply {
            this.width = 600
            this.setBackgroundDrawable(requireActivity().getDrawable(R.drawable.border_all_rounded_no_storke))
        }

        listPopupWindow.anchorView = anchorView
        val menu = PopupMenu(requireActivity(), anchorView).menu
        requireActivity().menuInflater.inflate(R.menu.inquiry_popup_menu, menu)

        val listPopupWindowAdapter = InquiryListPopupWindowAdapter(requireActivity(), menu)
        listPopupWindow.setAdapter(listPopupWindowAdapter)
        listPopupWindow.setOnItemClickListener{
            parent, view, position, id ->
            when(position){
                0 -> {
                    inquiryVM.setRemovable(true)
                    inquiryVM.setMoreView(true)
                }
                1 -> {
                    Log.d(TAG, "1111")
                }
                2 -> {
                    Log.d(TAG, "22222")
                }
            }
            listPopupWindow.dismiss()
        }
        listPopupWindow.show()
    }

    private fun setListener(){

        constraint_delete_final.setOnClickListener {
            ConfirmDeleteDialog(requireActivity()){
                dialog ->

                val selectedDataSoon = soonArrivalListAdapter.getSelectedListData()
                val selectedDataRegister = registeredSopoListAdapter.getSelectedListData()
                val selectedData = Stream.of(selectedDataSoon, selectedDataRegister).flatMap { it.stream() }
                    .collect(Collectors.toList())

                inquiryVM.removeItem(selectedData)

                soonArrivalListAdapter.deleteSelectedParcel()
                soonArrivalList = soonArrivalListAdapter.getList()

                registeredSopoListAdapter.deleteSelectedParcel()
                registeredSopoList = registeredSopoListAdapter.getList()

                viewSettingForSoonArrivalList(soonArrivalList.size)
                viewSettingForRegisteredList(registeredSopoList.size)

                inquiryVM.cancelRemoveItem()
                dialog.dismiss()
            }
                .show(requireActivity().supportFragmentManager, "ConfirmDeleteDialog")
        }
    }

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

    private fun viewSettingForSoonArrivalList(listSize: Int){
        when(listSize){
            0 -> {
                constraint_soon_arrival.visibility = View.GONE
                linear_more_view_parent.visibility = View.GONE
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

    private fun viewSettingforPopupMenuDelete(){
        tv_title.visibility = View.INVISIBLE
        constraint_select.visibility = View.INVISIBLE
        image_inquiry_popup_menu.visibility = View.INVISIBLE
        image_inquiry_popup_menu_close.visibility = View.VISIBLE
        linear_more_view_parent.visibility = View.GONE
        v_more_view.visibility = View.INVISIBLE
        constraint_delete_select.visibility = View.VISIBLE
        tv_delete_title.visibility = View.VISIBLE
    }

    private fun viewSettingforPopupMenuDelete_Cancel(){
        tv_title.visibility = View.VISIBLE
        constraint_select.visibility = View.VISIBLE
        image_inquiry_popup_menu.visibility = View.VISIBLE
        image_inquiry_popup_menu_close.visibility = View.INVISIBLE
        tv_delete_title.visibility = View.GONE
        constraint_delete_select.visibility = View.GONE

        viewSettingForSoonArrivalList(soonArrivalList.size)
        viewSettingForRegisteredList(registeredSopoList.size)
    }
}