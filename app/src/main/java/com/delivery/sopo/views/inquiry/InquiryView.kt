package com.delivery.sopo.views.inquiry

import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatus
import com.delivery.sopo.databinding.SopoInquiryViewBinding
import com.delivery.sopo.interfaces.BasicView
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
    }

    override fun setObserver(){
        inquiryVM.parcelList.observe(this, Observer {
            parcelList ->

            parcelList?.let{
                soonArrivalListAdapter.setParcel(parcelList.filter {
                    parcel ->
                    // 리스트 중 오직 '배송출발'일 경우만 해당 adapter로 넘긴다.
                    parcel.deliveryStatus == DeliveryStatus.OUT_FOR_DELIVERY
                }.also {
                    if(it.size >= 3){
                        linear_more_view_parent.visibility = View.VISIBLE
                    }
                })
                registeredSopoListAdapter.setParcel(parcelList.filter {
                    parcel ->
                    // 리스트 중 오직 '배송출발'과 '배송도착'이 아닐 경우만 해당 adapter로 넘긴다.
                    parcel.deliveryStatus != DeliveryStatus.OUT_FOR_DELIVERY && parcel.deliveryStatus != DeliveryStatus.DELIVERED
                })
            }
        })

        inquiryVM.isMoreView.observe(this, Observer{
            if(it){
                soonArrivalListAdapter.isFullListItem(true)
                linear_more_view.visibility = View.INVISIBLE
            }
            else{
                soonArrivalListAdapter.isFullListItem(false)
//                linear_more_view.visibility = View.VISIBLE
            }
        })
    }
}