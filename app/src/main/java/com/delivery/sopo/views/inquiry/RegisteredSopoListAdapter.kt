package com.delivery.sopo.views.inquiry

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatus
import com.delivery.sopo.databinding.InquiryListRegisteredItemBinding
import com.delivery.sopo.models.inquiry.InquiryListData
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import kotlinx.android.synthetic.main.inquiry_list_registered_item.view.*
import kotlinx.android.synthetic.main.inquiry_list_soon_item.view.*


class RegisteredSopoListAdapter(private val cntOfSelectedItem: MutableLiveData<Int>, lifecycleOwner: LifecycleOwner,
                                private var list: MutableList<InquiryListData>) : RecyclerView.Adapter<RegisteredSopoListAdapter.ViewHolder>()
{
    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    private var isRemovable = false

    init {
        cntOfSelectedItem.observe(lifecycleOwner, Observer {
            Log.d(TAG,"[3] @@ => $it")
        })
    }

    class ViewHolder(private val binding: InquiryListRegisteredItemBinding) : RecyclerView.ViewHolder(binding.root) {

        val registeredBinding = binding

        fun bind(inquiryListData: InquiryListData){
            binding.apply {
                registeredInquiryData = inquiryListData
            }
        }

    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.inquiry_list_registered_item,
                parent,
                false
            )
        )
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {

        val inquiryListData = list[position]

        holder.apply{
            bind(inquiryListData)
            itemView.tag = inquiryListData
        }

        val data: Parcel = list[position].parcel
        when(data.deliveryStatus){
            //상품 준비중
            DeliveryStatus.INFORMATION_RECEIVED -> {
                holder.registeredBinding.root.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_registered)
                holder.registeredBinding.root.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_300)
                holder.registeredBinding.root.tv_delivery_status.text = "송장등록"
            }
            //상품 인수
            DeliveryStatus.AT_PICKUP -> {
                holder.registeredBinding.root.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_before)
                holder.registeredBinding.root.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_GRAY_400)
                holder.registeredBinding.root.tv_delivery_status.text = "배송 전"
            }
            //상품 이동 중
            DeliveryStatus.IN_TRANSIT -> {
                holder.registeredBinding.root.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_ing)
                holder.registeredBinding.root.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_900)
                holder.registeredBinding.root.tv_delivery_status.text = "배송 중"
            }
            //배송 출발
            DeliveryStatus.OUT_FOR_DELIVERY -> {
                // Nothing to do!!
            }
            //배송 도착
            DeliveryStatus.DELIVERED -> {
                // Nothing to do!!
            }
        }
        holder.registeredBinding.root.tv_registered_parcel_name.text = data.parcelAlias
        holder.registeredBinding.root.tv_registered_parcel_date.text = data.auditDte.substring(0, data.auditDte.indexOf("T"))

        if(inquiryListData.isSelected){
            viewSettingForSelected(holder.registeredBinding)
        }
        else{
            viewInitialize(holder.registeredBinding)
        }

        holder.registeredBinding.root.cv_registered_parent.setOnClickListener {
            if(isRemovable && !inquiryListData.isSelected){
                inquiryListData.isSelected = true
                cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
                viewSettingForSelected(holder.registeredBinding)
            }
            else if (isRemovable && inquiryListData.isSelected){
                inquiryListData.isSelected = false
                cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) - 1
                viewInitialize(holder.registeredBinding)
            }
            else{
                Log.d(TAG, "33333")
            }
        }
    }

    fun setRemovable(flag: Boolean){
        isRemovable = flag
        notifyDataSetChanged()
    }

    fun setDataList(parcel: MutableList<InquiryListData>){
        this.list = parcel
        notifyDataSetChanged()
    }

    fun cancelRemoveItem(){
        for(item in list){
            item.isSelected = false
        }
    }

    fun setSelectAll(flag: Boolean){
        if(flag){
            for(item in list){
                if(!item.isSelected){
                    item.isSelected = true
                    cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
                }
            }
            notifyDataSetChanged()
        }
        else{
            for(item in list){
                item.isSelected = false
            }
            cntOfSelectedItem.value = 0
            notifyDataSetChanged()
        }
    }

    fun getSelectedListData(): List<ParcelId>
    {
        return list.filter {
            it.isSelected
        }.map {
            it.parcel.parcelId
        }
    }

    fun getList(): MutableList<InquiryListData> {
        return list
    }

    fun deleteSelectedParcel(){
        list.removeIf {
            it.isSelected
        }
        notifyDataSetChanged()
    }

    private fun viewSettingForSelected(binding: InquiryListRegisteredItemBinding){
        binding.root.constraint_delivery_status_front.visibility = View.GONE
        binding.root.constraint_delivery_status_back.visibility = View.GONE
        binding.root.constraint_delivery_status_front_delete.visibility = View.VISIBLE
        binding.root.constraint_delivery_status_back_delete.visibility = View.VISIBLE
    }

    private fun viewInitialize(binding: InquiryListRegisteredItemBinding){
        binding.root.constraint_delivery_status_front.visibility = View.VISIBLE
        binding.root.constraint_delivery_status_back.visibility = View.VISIBLE
        binding.root.constraint_delivery_status_front_delete.visibility = View.GONE
        binding.root.constraint_delivery_status_back_delete.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return list.size
    }
}