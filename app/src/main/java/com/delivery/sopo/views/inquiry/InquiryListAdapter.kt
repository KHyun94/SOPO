package com.delivery.sopo.views.inquiry

import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.databinding.InquiryListCompleteItemBinding
import com.delivery.sopo.databinding.InquiryListOngoingItemBinding
import com.delivery.sopo.enums.InquiryItemType
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.util.fun_util.SizeUtil
import kotlinx.android.synthetic.main.inquiry_list_complete_item.view.*
import kotlinx.android.synthetic.main.inquiry_list_ongoing_item.view.*

class InquiryListAdapter(private val cntOfSelectedItem: MutableLiveData<Int>, lifecycleOwner: LifecycleOwner,
                         private var list: MutableList<InquiryListItem>,
                         private val itemType: InquiryItemType) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    // TODO : CONST로 빼던지 BUILD_CONFIG로 빼야함.
    private val limitOfSoonListSize = 2

    private var isMoreView = false
    private var isRemovable = false

    init {
        cntOfSelectedItem.observe(lifecycleOwner, Observer {})
    }

    class OngoingViewHolder(private val binding: InquiryListOngoingItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val ongoingBinding = binding

        fun bind(inquiryListItem: InquiryListItem){
            binding.apply {
                ongoingInquiryData = inquiryListItem
            }
        }
    }

    class CompleteViewHolder(private val binding: InquiryListCompleteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val completeBinding = binding

        fun bind(inquiryListItem: InquiryListItem){
            binding.apply {
                completeInquiryData = inquiryListItem
            }
        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(itemType){
            InquiryItemType.Soon ->{
                return OngoingViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.inquiry_list_ongoing_item, parent, false)
                )
            }
            InquiryItemType.Registered ->{
                return OngoingViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.inquiry_list_ongoing_item, parent, false)
                )
            }
            InquiryItemType.Complete -> {
                return CompleteViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.inquiry_list_complete_item, parent, false)
                )
            }
        }
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val inquiryListData = list[position]

        when(holder){
            is OngoingViewHolder -> {
                holder.bind(inquiryListData)
                holder.itemView.tag = inquiryListData

                val data: Parcel = list[position].parcel
                when(data.deliveryStatus){
                    //상품 준비중
                    DeliveryStatusEnum.information_received.code -> {
                        holder.ongoingBinding.root.apply {
                            this.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_registered)
                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_300)
                            this.tv_delivery_status.text = "송장등록"
                            this.tv_delivery_status.setTextColor(ContextCompat.getColor(this.context, R.color.MAIN_WHITE))
                        }
                    }
                    //상품 인수
                    DeliveryStatusEnum.at_pickup.code -> {
                        holder.ongoingBinding.root.apply {
                            this.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_before)
                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_GRAY_50)
                            this.tv_delivery_status.text = "배송전"
                            this.tv_delivery_status.setTextColor(ContextCompat.getColor(this.context, R.color.COLOR_GRAY_300))
                        }
                    }
                    //상품 이동 중
                    DeliveryStatusEnum.in_transit.code -> {
                        holder.ongoingBinding.root.apply {
                            this.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_ing)
                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_900)
                            this.tv_delivery_status.text = "배송중"
                            this.tv_delivery_status.setTextColor(ContextCompat.getColor(this.context, R.color.MAIN_WHITE))
                        }
                    }
                    //배송 출발
                    DeliveryStatusEnum.out_for_delivery.code -> {
                        holder.ongoingBinding.root.apply {
                            Glide.with(this.context).asGif().load(R.drawable.start_delivery).into(this.image_delivery_status)
                            val gifMargin = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                            gifMargin.setMargins(0,0,0,-SizeUtil.changeDpToPx(this.context, 8F))
                            gifMargin.height = SizeUtil.changeDpToPx(this.context, 38F)
                            gifMargin.width = SizeUtil.changeDpToPx(this.context, 50F)

//                            this.image_delivery_status.layoutParams.height = SizeUtil.changeDpToPx(this.context, 50F)
//                            this.image_delivery_status.layoutParams.width = SizeUtil.changeDpToPx(this.context, 50F)
//                            this.image_delivery_status.requestLayout()
                            this.image_delivery_status.layoutParams = gifMargin
//                            this.image_delivery_status.requestLayout()

                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_700)
                            this.tv_delivery_status.text = "배송출발"
                            this.tv_delivery_status.setTextColor(ContextCompat.getColor(holder.ongoingBinding.root.context, R.color.MAIN_WHITE))
                        }
                    }
                    //배송 도착
                    DeliveryStatusEnum.delivered.code -> {
                        // Nothing to do!!
                    }
                    else -> {
                        holder.ongoingBinding.root.apply {
                            this.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_registered)
                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_300)
                            this.tv_delivery_status.text = "송장등록"
                            this.tv_delivery_status.setTextColor(ContextCompat.getColor(this.context, R.color.MAIN_WHITE))
                        }
                    }
                }

                if(inquiryListData.isSelected){
                    ongoingViewSelected(holder.ongoingBinding)
                }
                else{
                    ongoingViewInitialize(holder.ongoingBinding)
                }

                holder.ongoingBinding.root.cv_ongoing_parent.setOnClickListener {
                    if(isRemovable && !inquiryListData.isSelected){
                        inquiryListData.isSelected = true
                        cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
                        ongoingViewSelected(holder.ongoingBinding)
                    }
                    else if (isRemovable && inquiryListData.isSelected){
                        inquiryListData.isSelected = false
                        cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) - 1
                        ongoingViewInitialize(holder.ongoingBinding)
                    }
                    else{
                        Log.d(TAG, "33333")
                    }
                }
            }
            is CompleteViewHolder -> {
                holder.bind(inquiryListData)
                holder.itemView.tag = inquiryListData

                if(inquiryListData.isSelected){
                    completeViewSelected(holder.completeBinding)
                }
                else{
                    completeViewInitialize(holder.completeBinding)
                }
                holder.completeBinding.root.cv_complete_parent.setOnClickListener{

                    Log.d(TAG, "isSelect : ${inquiryListData.isSelected} && isRemovable : $isRemovable")

                    if(isRemovable && !inquiryListData.isSelected){
                        inquiryListData.isSelected = true
                        cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
                        completeViewSelected(holder.completeBinding)
                    }
                    else if (isRemovable && inquiryListData.isSelected){
                        inquiryListData.isSelected = false
                        cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) - 1
                        completeViewInitialize(holder.completeBinding)
                    }
                    else{
                        Log.d(TAG, "2122")
                    }
                }
            }
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

    private fun ongoingViewSelected(binding: InquiryListOngoingItemBinding){
        binding.root.constraint_delivery_status_front.visibility = GONE
        binding.root.constraint_delivery_status_back.visibility = GONE
        binding.root.constraint_delivery_status_front_delete.visibility = VISIBLE
        binding.root.constraint_delivery_status_back_delete.visibility = VISIBLE
        binding.root.linear_parent_list_item_register.background = ContextCompat.getDrawable(binding.root.context, R.drawable.border_red)
    }

    private fun ongoingViewInitialize(binding: InquiryListOngoingItemBinding){
        binding.root.constraint_delivery_status_front.visibility = VISIBLE
        binding.root.constraint_delivery_status_back.visibility = VISIBLE
        binding.root.constraint_delivery_status_front_delete.visibility = GONE
        binding.root.constraint_delivery_status_back_delete.visibility = GONE
        binding.root.linear_parent_list_item_register.background = null
    }

    private fun completeViewSelected(binding: InquiryListCompleteItemBinding){
        binding.root.constraint_date_complete.visibility = GONE
        binding.root.constraint_item_part_complete.visibility = GONE
        binding.root.v_dividerLine.visibility = GONE
        binding.root.constraint_delivery_status_front_complete.visibility = VISIBLE
        binding.root.constraint_item_part_delete_complete.visibility = VISIBLE
        binding.root.linear_item_complete.background = ContextCompat.getDrawable(binding.root.context, R.drawable.border_red)
    }

    private fun completeViewInitialize(binding: InquiryListCompleteItemBinding){
        binding.root.constraint_item_part_complete.visibility = VISIBLE
        binding.root.constraint_date_complete.visibility = VISIBLE
        binding.root.v_dividerLine.visibility = VISIBLE
        binding.root.constraint_item_part_delete_complete.visibility = GONE
        binding.root.constraint_delivery_status_front_complete.visibility = GONE
        binding.root.linear_item_complete.background = null
    }

    fun setRemovable(flag: Boolean){
        isRemovable = flag
        if(!isRemovable){
            for(item in list){
                item.isSelected = false
            }
        }
        notifyDataSetChanged()
    }

    fun addItems(listItem: MutableList<InquiryListItem>){
        if(listItem.size > 0){
            list.addAll(listItem)
            notifyDataSetChanged()
        }
    }

    fun setDataList(listItem: MutableList<InquiryListItem>) {

        this.list = when(itemType){
            InquiryItemType.Soon -> {
                listItem.filter {
                    it.parcel.deliveryStatus == DeliveryStatusEnum.out_for_delivery.code
                }.toMutableList()
            }
            InquiryItemType.Registered -> {
                listItem.filter{
                    it.parcel.deliveryStatus != DeliveryStatusEnum.out_for_delivery.code && it.parcel.deliveryStatus != DeliveryStatusEnum.delivered.code
                }.toMutableList()
            }
            InquiryItemType.Complete -> {
                listItem.filter {
                    it.parcel.deliveryStatus == DeliveryStatusEnum.delivered.code
                }.toMutableList()
            }
        }
        notifyDataSetChanged()
    }

    fun isFullListItem(isFull : Boolean){
        this.isMoreView = isFull
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return when(itemType){
            InquiryItemType.Soon -> {
                list.let {
                    if(it.size > limitOfSoonListSize && !isMoreView){
                        limitOfSoonListSize
                    }
                    else {
                        it.size
                    }
                }
            }
            else ->{
                list.size
            }
        }
    }

    fun getListSize(): Int{
        return list.size
    }
}