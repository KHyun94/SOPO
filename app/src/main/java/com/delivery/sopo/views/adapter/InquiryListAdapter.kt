package com.delivery.sopo.views.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.delivery.sopo.databinding.InquiryListCompleteItemBinding
import com.delivery.sopo.databinding.InquiryListOngoingItemBinding
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.InquiryItemTypeEnum
import com.delivery.sopo.interfaces.listener.OnParcelClickListener
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.util.SizeUtil
import kotlinx.android.synthetic.main.inquiry_list_complete_item.view.*
import kotlinx.android.synthetic.main.inquiry_list_ongoing_item.view.*

class InquiryListAdapter(
    private val cntOfSelectedItem: MutableLiveData<Int>,
    lifecycleOwner: LifecycleOwner,
    private var list: MutableList<InquiryListItem>,
    private val itemTypeEnum: InquiryItemTypeEnum
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private var mClickListener: OnParcelClickListener? = null

    fun setOnParcelClickListener(_mClickListener: OnParcelClickListener)
    {
        mClickListener = _mClickListener
    }

    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    // TODO : CONST로 빼던지 BUILD_CONFIG로 빼야함.
    private val limitOfSoonListSize = 2

    private var isMoreView = false
    private var isRemovable = false

    init
    {
        cntOfSelectedItem.observe(lifecycleOwner, Observer {})
    }

    class OngoingViewHolder(private val binding: InquiryListOngoingItemBinding) :
        RecyclerView.ViewHolder(binding.root)
    {
        val ongoingBinding = binding

        fun bind(inquiryListItem: InquiryListItem)
        {
            binding.apply {
                ongoingInquiryData = inquiryListItem
            }
        }
    }

    class CompleteViewHolder(private val binding: InquiryListCompleteItemBinding) :
        RecyclerView.ViewHolder(binding.root)
    {
        val completeBinding = binding

        fun bind(inquiryListItem: InquiryListItem)
        {
            binding.apply {
                completeInquiryData = inquiryListItem
            }
        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        when (itemTypeEnum)
        {
            InquiryItemTypeEnum.Soon ->
            {
                return OngoingViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.inquiry_list_ongoing_item,
                        parent,
                        false
                    )
                )
            }
            InquiryItemTypeEnum.Registered ->
            {
                return OngoingViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.inquiry_list_ongoing_item,
                        parent,
                        false
                    )
                )
            }
            InquiryItemTypeEnum.Complete ->
            {
                return CompleteViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.inquiry_list_complete_item,
                        parent,
                        false
                    )
                )
            }
        }
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val inquiryListData = list[position]

        when (holder)
        {
            is OngoingViewHolder ->
            {
                holder.bind(inquiryListData)
                holder.itemView.tag = inquiryListData

                val data: Parcel = list[position].parcel
                when (data.deliveryStatus)
                {
                    //상품 준비중
                    DeliveryStatusEnum.information_received.code ->
                    {
                        holder.ongoingBinding.root.apply {
                            this.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_registered)
                            this.iv_red_dot.visibility = if(inquiryListData.isUpdated) View.GONE else View.VISIBLE
                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_300)
                            this.tv_delivery_status.text = "송장등록"
                            this.tv_delivery_status.setTextColor(
                                ContextCompat.getColor(
                                    this.context,
                                    R.color.MAIN_WHITE
                                )
                            )
                        }
                    }
                    //상품 인수
                    DeliveryStatusEnum.at_pickup.code ->
                    {
                        holder.ongoingBinding.root.apply {
                            this.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_before)
                            this.iv_red_dot.visibility = if(inquiryListData.isUpdated) View.GONE else View.VISIBLE
                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_GRAY_50)
                            this.tv_delivery_status.text = "배송전"
                            this.tv_delivery_status.setTextColor(
                                ContextCompat.getColor(
                                    this.context,
                                    R.color.COLOR_GRAY_300
                                )
                            )
                        }
                    }
                    //상품 이동 중
                    DeliveryStatusEnum.in_transit.code ->
                    {
                        holder.ongoingBinding.root.apply {
                            this.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_ing)
                            this.iv_red_dot.visibility = if(inquiryListData.isUpdated) View.GONE else View.VISIBLE
                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_900)
                            this.tv_delivery_status.text = "배송중"
                            this.tv_delivery_status.setTextColor(
                                ContextCompat.getColor(
                                    this.context,
                                    R.color.MAIN_WHITE
                                )
                            )
                        }
                    }
                    //배송 출발
                    DeliveryStatusEnum.out_for_delivery.code ->
                    {
                        holder.ongoingBinding.root.apply {
                            Glide.with(this.context).asGif().load(R.drawable.start_delivery)
                                .into(this.image_delivery_status)
                            val gifMargin = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            gifMargin.height = SizeUtil.changeDpToPx(this.context, 38F)
                            gifMargin.width = SizeUtil.changeDpToPx(this.context, 50F)
                            this.image_delivery_status.layoutParams = gifMargin
                            this.iv_red_dot.visibility = if(inquiryListData.isUpdated) View.GONE else View.VISIBLE
                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_700)
                            this.tv_delivery_status.text = "배송출발"
                            this.tv_delivery_status.setTextColor(
                                ContextCompat.getColor(
                                    holder.ongoingBinding.root.context,
                                    R.color.MAIN_WHITE
                                )
                            )
                        }
                    }
                    //배송 도착
                    DeliveryStatusEnum.delivered.code ->
                    {
                        // Nothing to do!!
                    }
                    else ->
                    {
                        holder.ongoingBinding.root.apply {
                            this.image_delivery_status.setBackgroundResource(R.drawable.ic_parcel_status_registered)
                            this.iv_red_dot.visibility = if(inquiryListData.isUpdated) View.GONE else View.VISIBLE
                            this.constraint_delivery_status_front.setBackgroundResource(R.color.COLOR_MAIN_300)
                            this.tv_delivery_status.text = "송장등록"
                            this.tv_delivery_status.setTextColor(
                                ContextCompat.getColor(
                                    this.context,
                                    R.color.MAIN_WHITE
                                )
                            )
                        }
                    }
                }

                if (inquiryListData.isSelected)
                {
                    ongoingViewSelected(holder.ongoingBinding)
                }
                else
                {
                    ongoingViewInitialize(holder.ongoingBinding)
                }

                // v: View , isRemoveable : Boolean, item : InquiryListItem

                val onGoingView = holder.ongoingBinding.root.cv_ongoing_parent

                holder.ongoingBinding.root.cv_ongoing_parent.setOnClickListener {
                    if (isRemovable && !inquiryListData.isSelected)
                    {
                        inquiryListData.isSelected = true
                        cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
                        ongoingViewSelected(holder.ongoingBinding)
                    }
                    else if (isRemovable && inquiryListData.isSelected)
                    {
                        inquiryListData.isSelected = false
                        cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) - 1
                        ongoingViewInitialize(holder.ongoingBinding)
                    }
                    else
                    {
                        Log.d(TAG, "33333")

                        if (mClickListener != null)
                        {
                            mClickListener!!.onItemClicked(
                                view = it,
                                type = 0,
                                parcelId = inquiryListData.parcel.parcelId
                            )
                        }
                    }
                }

                holder.itemView.cv_ongoing_parent.setOnLongClickListener {
                    if (mClickListener != null)
                    {
                        mClickListener!!.onItemLongClicked(
                            view = it,
                            type = 0,
                            parcelId = inquiryListData.parcel.parcelId
                        )
                    }
                    return@setOnLongClickListener true
                }
            }
            is CompleteViewHolder ->
            {
                holder.bind(inquiryListData)
                holder.itemView.tag = inquiryListData

                if (inquiryListData.isSelected)
                {
                    completeViewSelected(holder.completeBinding)
                }
                else
                {
                    completeViewInitialize(holder.completeBinding)
                }
                holder.completeBinding.root.cv_complete_parent.setOnClickListener {

                    Log.d(
                        TAG,
                        "isSelect : ${inquiryListData.isSelected} && isRemovable : $isRemovable"
                    )

                    if (isRemovable && !inquiryListData.isSelected)
                    {
                        inquiryListData.isSelected = true
                        cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
                        completeViewSelected(holder.completeBinding)
                    }
                    else if (isRemovable && inquiryListData.isSelected)
                    {
                        inquiryListData.isSelected = false
                        cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) - 1
                        completeViewInitialize(holder.completeBinding)
                    }
                    else
                    {
                        if (mClickListener != null)
                        {
                            mClickListener!!.onItemClicked(
                                view = it,
                                type = 1,
                                parcelId = inquiryListData.parcel.parcelId
                            )
                        }
                    }
                }

                holder.itemView.cv_complete_parent.setOnLongClickListener {
                    if (mClickListener != null)
                    {
                        mClickListener!!.onItemLongClicked(
                            view = it,
                            type = 1,
                            parcelId = inquiryListData.parcel.parcelId
                        )
                    }
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun setSelectAll(flag: Boolean)
    {
        if (flag)
        {
            for (item in list)
            {
                if (!item.isSelected)
                {
                    item.isSelected = true
                    cntOfSelectedItem.value = (cntOfSelectedItem.value ?: 0) + 1
                }
            }
            notifyDataSetChanged()
        }
        else
        {
            for (item in list)
            {
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

    private fun ongoingViewSelected(binding: InquiryListOngoingItemBinding)
    {
        binding.root.constraint_delivery_status_front.visibility = GONE
        binding.root.constraint_delivery_status_back.visibility = GONE
        binding.root.constraint_delivery_status_front_delete.visibility = VISIBLE
        binding.root.constraint_delivery_status_back_delete.visibility = VISIBLE
        binding.root.linear_parent_list_item_register.background =
            ContextCompat.getDrawable(binding.root.context, R.drawable.border_red)
    }

    private fun ongoingViewInitialize(binding: InquiryListOngoingItemBinding)
    {
        binding.root.constraint_delivery_status_front.visibility = VISIBLE
        binding.root.constraint_delivery_status_back.visibility = VISIBLE
        binding.root.constraint_delivery_status_front_delete.visibility = GONE
        binding.root.constraint_delivery_status_back_delete.visibility = GONE
        binding.root.linear_parent_list_item_register.background = null
    }

    private fun completeViewSelected(binding: InquiryListCompleteItemBinding)
    {
        binding.root.constraint_date_complete.visibility = GONE
        binding.root.constraint_item_part_complete.visibility = GONE
        binding.root.v_dividerLine.visibility = GONE
        binding.root.constraint_delivery_status_front_complete.visibility = VISIBLE
        binding.root.constraint_item_part_delete_complete.visibility = VISIBLE
        binding.root.linear_item_complete.background =
            ContextCompat.getDrawable(binding.root.context, R.drawable.border_red)
    }

    private fun completeViewInitialize(binding: InquiryListCompleteItemBinding)
    {
        binding.root.constraint_item_part_complete.visibility = VISIBLE
        binding.root.constraint_date_complete.visibility = VISIBLE
        binding.root.v_dividerLine.visibility = VISIBLE
        binding.root.constraint_item_part_delete_complete.visibility = GONE
        binding.root.constraint_delivery_status_front_complete.visibility = GONE
        binding.root.linear_item_complete.background = null
    }

    fun setRemovable(flag: Boolean)
    {
        isRemovable = flag
        if (!isRemovable)
        {
            for (item in list)
            {
                item.isSelected = false
            }
        }
        notifyDataSetChanged()
    }

    //현재는 '배송완료'에만 적용되어있음. 데이터를 무조건 notifyDataSetChanged()로 데이터를 리프레쉬하지 않고 진짜 변경된 데이터만 변경할 수 있도록함.
    fun notifyChanged(updatedList: MutableList<InquiryListItem>)
    {
        updatedList.sortByDescending { it.parcel.arrivalDte }

        if (list.size > updatedList.size)
        {
            list.removeIf { list.indexOf(it) > updatedList.lastIndex }
            notifyDataSetChanged()
        }

        val notifyIndexList = mutableListOf<Int>()
        for (index in 0..updatedList.lastIndex)
        {
            if (list.getOrNull(index) == null)
            {
                Log.d(
                    TAG,
                    "기존 리스트에 해당 index[$index]가 존재하지 않아 list[$index]에 ${updatedList[index].parcel.parcelAlias} 아이템을 추가합니다."
                )
                list.add(updatedList[index])
                notifyIndexList.add(index)
            }
            else if (!((updatedList[index].parcel.parcelId.regDt == list[index].parcel.parcelId.regDt) && (updatedList[index].parcel.parcelId.parcelUid == list[index].parcel.parcelId.parcelUid)))
            {
                Log.d(
                    TAG,
                    "index[$index]에 해당하는 ${list[index].parcel.parcelAlias}와 업데이트될 아이템(${updatedList[index].parcel.parcelAlias}) 일치하지 않아 기존 아이템에 업데이트될 아이템을 덮어씁니다."
                )
                list[index] = updatedList[index]
                notifyIndexList.add(index)
            }
        }

        for (index in 0..notifyIndexList.lastIndex)
        {
            notifyItemChanged(notifyIndexList[index])
        }
    }

    fun setDataList(listItem: MutableList<InquiryListItem>)
    {
        this.list = when (itemTypeEnum)
        {
            InquiryItemTypeEnum.Soon ->
            {
                listItem.filter {
                    it.isUpdated = false
                    it.parcel.deliveryStatus == DeliveryStatusEnum.out_for_delivery.code
                }.toMutableList()
            }
            InquiryItemTypeEnum.Registered ->
            {
                listItem.filter {
                    it.isUpdated = false
                    it.parcel.deliveryStatus != DeliveryStatusEnum.out_for_delivery.code && it.parcel.deliveryStatus != DeliveryStatusEnum.delivered.code
                }.toMutableList()
            }
            InquiryItemTypeEnum.Complete ->
            {
                listItem.filter {
                    it.isUpdated = true
                    it.parcel.deliveryStatus == DeliveryStatusEnum.delivered.code
                }.toMutableList()
            }
        }
        notifyDataSetChanged()
    }

    fun isFullListItem(isFull: Boolean)
    {
        this.isMoreView = isFull
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int
    {
        return when (itemTypeEnum)
        {
            InquiryItemTypeEnum.Soon ->
            {
                list.let {
                    if (it.size > limitOfSoonListSize && !isMoreView)
                    {
                        limitOfSoonListSize
                    }
                    else
                    {
                        it.size
                    }
                }
            }
            else ->
            {
                list.size
            }
        }
    }

    fun getListSize(): Int
    {
        return list.size
    }

    fun getList(): MutableList<InquiryListItem>
    {
        return list
    }
}