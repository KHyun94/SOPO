package com.delivery.sopo.views.inquiry

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatus
import com.delivery.sopo.databinding.SopoInquiryViewBinding
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.models.inquiry.InquiryListData
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import kotlinx.android.synthetic.main.sopo_inquiry_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class InquiryView : BasicView<SopoInquiryViewBinding>(R.layout.sopo_inquiry_view)
{
    private val soonArrivalListAdapter = SoonArrivalListAdapter(null)
    private val registeredSopoListAdapter = RegisteredSopoListAdapter(null)

    private val inquiryVM: InquiryViewModel by viewModel()

    init {
        TAG += this.javaClass.simpleName
        parentActivity = this@InquiryView
    }

    override fun bindView() {
        binding.vm = inquiryVM
        binding.recyclerviewSoonArrival.adapter = soonArrivalListAdapter
        binding.recyclerviewSoonArrival.layoutManager = LinearLayoutManager(this)

        binding.recyclerviewRegisteredParcel.adapter = registeredSopoListAdapter
        binding.recyclerviewRegisteredParcel.layoutManager = LinearLayoutManager(this)
        binding.executePendingBindings()

        image_inquiry_popup_menu.setOnClickListener {
            showListPopupWindow(it)
//            showPopupMenu(it)
        }
    }

    override fun setObserver(){
        inquiryVM.parcelList.observe(this, Observer {
            parcelList ->

            parcelList?.let{
                soonArrivalListAdapter.setDataList(parcelList.filter { parcel ->
                    // 리스트 중 오직 '배송출발'일 경우만 해당 adapter로 넘긴다.
                    parcel.deliveryStatus == DeliveryStatus.OUT_FOR_DELIVERY
                }.also {
                    when(it.size){
                        0 -> {
                            constraint_soon_arrival.visibility = View.GONE
                            linear_more_view_parent.visibility = View.GONE
                        }
                        1-> {
                            constraint_soon_arrival.visibility = View.VISIBLE
                            linear_more_view_parent.visibility = View.INVISIBLE
                        }
                        2-> {
                            constraint_soon_arrival.visibility = View.VISIBLE
                            linear_more_view_parent.visibility = View.INVISIBLE
                        }
                        else -> {
                            constraint_soon_arrival.visibility = View.VISIBLE
                            linear_more_view_parent.visibility = View.VISIBLE
                        }
                    }
                }.map{
                    InquiryListData(parcel = it)
                } as MutableList<InquiryListData>)
                registeredSopoListAdapter.setDataList(parcelList.filter { parcel ->
                    // 리스트 중 오직 '배송출발'과 '배송도착'이 아닐 경우만 해당 adapter로 넘긴다.
                    parcel.deliveryStatus != DeliveryStatus.OUT_FOR_DELIVERY && parcel.deliveryStatus != DeliveryStatus.DELIVERED
                }.map {
                    InquiryListData(parcel = it)
                } as MutableList<InquiryListData>)
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
    }

    private fun showListPopupWindow(anchorView: View){

        val listPopupWindow = ListPopupWindow(this).apply {
            this.width = 600
            this.setBackgroundDrawable(parentActivity.getDrawable(R.drawable.border_all_rounded_no_storke))
        }

        listPopupWindow.anchorView = anchorView
        val menu = PopupMenu(this, anchorView).menu
        menuInflater.inflate(R.menu.inquiry_popup_menu, menu)

        val listPopupWindowAdapter = InquiryListPopupWindowAdapter(this, menu)
        listPopupWindow.setAdapter(listPopupWindowAdapter)
        listPopupWindow.setOnItemClickListener{
            parent, view, position, id ->
            when(position){
                0 -> {
                    constraint_select.visibility = View.INVISIBLE
                    constraint_delete.visibility = View.VISIBLE
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

    private fun showPopupMenu(v: View){
        val context = ContextThemeWrapper(this, R.style.PopupMenuListView)
        val popupMenu = PopupMenu(context, v)
        menuInflater.inflate(R.menu.inquiry_popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {

            when(it.itemId){
                R.id.delete_item ->{
                    Log.d(TAG, "!!!!!!!!!!!!!!!!")
                }
            }

            false
        })
        popupMenu.show()
    }
}