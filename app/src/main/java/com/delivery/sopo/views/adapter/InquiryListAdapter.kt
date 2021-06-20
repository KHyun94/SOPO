package com.delivery.sopo.views.adapter

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.InquiryListCompleteItemBinding
import com.delivery.sopo.databinding.InquiryListOngoingItemBinding
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.InquiryItemTypeEnum
import com.delivery.sopo.interfaces.listener.OnParcelClickListener
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.ParcelDTO
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.data.repository.local.repository.ParcelLocalRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.android.synthetic.main.inquiry_list_complete_item.view.*
import kotlinx.android.synthetic.main.inquiry_list_ongoing_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class InquiryListAdapter(private val cntOfSelectedItemForDelete: MutableLiveData<Int>, private var list: MutableList<InquiryListItem> = mutableListOf(), private val itemTypeEnum: InquiryItemTypeEnum): RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent
{
    private val parcelLocalRepository: ParcelLocalRepository by inject()
    private var mClickListener: OnParcelClickListener? = null

    private val limitOfSoonListSize = 2

    private var isMoreView = false
    private var isRemovable = false

    fun setOnParcelClickListener(_mClickListener: OnParcelClickListener)
    {
        mClickListener = _mClickListener
    }

    inner class ProcessStatusViewHolder<T:ViewDataBinding>(private val _binding: T): RecyclerView.ViewHolder(_binding.root)
    {
        val binding: T = _binding

        fun bind(inquiryListItem: InquiryListItem)
        {
            binding.setVariable(BR.item, inquiryListItem)
        }
    }

    inner class OngoingViewHolder(private val binding: InquiryListOngoingItemBinding): RecyclerView.ViewHolder(binding.root)
    {
        var ongoingBinding: InquiryListOngoingItemBinding = binding

        fun bind(inquiryListItem: InquiryListItem)
        {
            ongoingBinding.setVariable(BR.ongoingInquiryData, inquiryListItem)
        }
    }

    inner class CompleteViewHolder(private val binding: InquiryListCompleteItemBinding): RecyclerView.ViewHolder(binding.root)
    {
        val completeBinding: InquiryListCompleteItemBinding = binding

        fun bind(inquiryListItem: InquiryListItem)
        {
            completeBinding.completeInquiryData = inquiryListItem
        }
    }

    private fun <T: ViewDataBinding> getBinding(inflater: LayoutInflater,
                                                @LayoutRes layoutRes: Int, parent: ViewGroup): T
    {
        return DataBindingUtil.inflate<T>(LayoutInflater.from(parent.context), layoutRes, parent, false)
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        when (itemTypeEnum)
        {
            InquiryItemTypeEnum.Soon ->
            {
                val binding =
                    getBinding<InquiryListOngoingItemBinding>(LayoutInflater.from(parent.context), R.layout.inquiry_list_ongoing_item, parent)
                return OngoingViewHolder(binding)
            }
            InquiryItemTypeEnum.Registered ->
            {
                val binding =
                    getBinding<InquiryListOngoingItemBinding>(LayoutInflater.from(parent.context), R.layout.inquiry_list_ongoing_item, parent)
                return OngoingViewHolder(binding)
            }
            InquiryItemTypeEnum.Complete ->
            {
                val binding =
                    getBinding<InquiryListCompleteItemBinding>(LayoutInflater.from(parent.context), R.layout.inquiry_list_complete_item, parent)
                return CompleteViewHolder(binding)
            }
        }
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val inquiryListItem: InquiryListItem = list[position]

        when (holder)
        {
            is OngoingViewHolder ->
            {
                holder.bind(inquiryListItem)
                holder.itemView.tag = inquiryListItem

                val parcelDTO: ParcelDTO = inquiryListItem.parcelDTO

                holder.ongoingBinding.tvDeliveryStatus.bringToFront()
                
                if (inquiryListItem.isSelected)
                {
                    ongoingViewSelected(holder.ongoingBinding)
                }
                else
                {
                    ongoingViewInitialize(holder.ongoingBinding)
                }

                // v: View , isRemoveable : Boolean, item : InquiryListItem
                holder.ongoingBinding.root.cv_ongoing_parent.setOnClickListener {
                    if (isRemovable && !inquiryListItem.isSelected)
                    {
                        inquiryListItem.isSelected = true
                        cntOfSelectedItemForDelete.value =
                            (cntOfSelectedItemForDelete.value ?: 0) + 1
                        ongoingViewSelected(holder.ongoingBinding)
                    }
                    else if (isRemovable && inquiryListItem.isSelected)
                    {
                        inquiryListItem.isSelected = false
                        cntOfSelectedItemForDelete.value =
                            (cntOfSelectedItemForDelete.value ?: 0) - 1
                        ongoingViewInitialize(holder.ongoingBinding)
                    }
                    else
                    {
                        if (mClickListener != null)
                        {
                            mClickListener!!.onItemClicked(
                                view = it, type = 0, parcelId = inquiryListItem.parcelDTO.parcelId
                            )
                        }
                    }
                }

                holder.itemView.cv_ongoing_parent.setOnLongClickListener {
                    if (!isRemovable && mClickListener != null)
                    {
                        mClickListener!!.onItemLongClicked(
                            view = it, type = 0, parcelId = inquiryListItem.parcelDTO.parcelId
                        )
                    }
                    return@setOnLongClickListener true
                }
            }
            is CompleteViewHolder ->
            {
                holder.bind(inquiryListItem)
                holder.itemView.tag = inquiryListItem

                if (inquiryListItem.isSelected)
                {
                    completeViewSelected(holder.completeBinding)
                }
                else
                {
                    completeViewInitialize(holder.completeBinding)
                }
                holder.completeBinding.root.cv_complete_parent.setOnClickListener {

                    SopoLog.d("isSelect : ${inquiryListItem.isSelected} && isRemovable : $isRemovable")

                    if (isRemovable && !inquiryListItem.isSelected)
                    {
                        inquiryListItem.isSelected = true
                        cntOfSelectedItemForDelete.value =
                            (cntOfSelectedItemForDelete.value ?: 0) + 1
                        completeViewSelected(holder.completeBinding)
                    }
                    else if (isRemovable && inquiryListItem.isSelected)
                    {
                        inquiryListItem.isSelected = false
                        cntOfSelectedItemForDelete.value =
                            (cntOfSelectedItemForDelete.value ?: 0) - 1
                        completeViewInitialize(holder.completeBinding)
                    }
                    else
                    {
                        if (mClickListener != null)
                        {
                            mClickListener!!.onItemClicked(
                                view = it, type = 1, parcelId = inquiryListItem.parcelDTO.parcelId
                            )
                        }
                    }
                }

                holder.itemView.cv_complete_parent.setOnLongClickListener {
                    if (!isRemovable && mClickListener != null)
                    {
                        mClickListener!!.onItemLongClicked(
                            view = it, type = 1, parcelId = inquiryListItem.parcelDTO.parcelId
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
                    cntOfSelectedItemForDelete.value = (cntOfSelectedItemForDelete.value ?: 0) + 1
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
            cntOfSelectedItemForDelete.value = 0
            notifyDataSetChanged()
        }
    }

    fun getSelectedListData(): List<ParcelId>
    {
        return list.filter {
            it.isSelected
        }.map {
            it.parcelDTO.parcelId
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
        updatedList.sortByDescending { it.parcelDTO.arrivalDte }

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
                SopoLog.d("기존 리스트에 해당 index[$index]가 존재하지 않아 list[$index]에 ${updatedList[index].parcelDTO.alias} 아이템을 추가합니다.")
                list.add(updatedList[index])
                notifyIndexList.add(index)
            }
            else if (!((updatedList[index].parcelDTO.parcelId.regDt == list[index].parcelDTO.parcelId.regDt) && (updatedList[index].parcelDTO.parcelId.parcelUid == list[index].parcelDTO.parcelId.parcelUid)))
            {
                SopoLog.d("index[$index]에 해당하는 ${list[index].parcelDTO.alias}와 업데이트될 아이템(${updatedList[index].parcelDTO.alias}) 일치하지 않아 기존 아이템에 업데이트될 아이템을 덮어씁니다.")
                list[index] = updatedList[index]
                notifyIndexList.add(index)
            }
        }

        for (index in 0..notifyIndexList.lastIndex)
        {
            notifyItemChanged(notifyIndexList[index])
        }
    }

    // 택배 리스트를 상태에 따라 분류
    fun separateDeliveryListByStatus(list: MutableList<InquiryListItem>?)
    {
        if (list == null) return

        this.list = when (itemTypeEnum)
        {
            InquiryItemTypeEnum.Soon ->
            {
                list.filter {
                    it.parcelDTO.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE
                }.toMutableList()
            }
            InquiryItemTypeEnum.Registered ->
            {
                list.filter {
                    it.parcelDTO.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE && it.parcelDTO.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE
                }.toMutableList()
            }
            InquiryItemTypeEnum.Complete ->
            {
                list.filter {
                    it.parcelDTO.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE
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