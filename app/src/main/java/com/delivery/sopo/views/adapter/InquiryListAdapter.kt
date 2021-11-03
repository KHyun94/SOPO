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
import com.delivery.sopo.util.SopoLog
import org.koin.core.KoinComponent

class InquiryListAdapter(private var list: MutableList<InquiryListItem> = mutableListOf(), private val parcelType: InquiryItemTypeEnum, private val cntOfSelectedItemForDelete: MutableLiveData<Int>? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent
{
    private var onParcelClickListener: OnParcelClickListener? = null

    private val limitOfSoonListSize = 2

    private var isMoreView = false
    private var isRemovable = false

    fun setOnParcelClickListener(onParcelClickListener: OnParcelClickListener)
    {
        this.onParcelClickListener = onParcelClickListener
    }

    inner class OngoingViewHolder(private val binding: InquiryListOngoingItemBinding): RecyclerView.ViewHolder(binding.root)
    {
        val ongoingBinding: InquiryListOngoingItemBinding = binding

        fun bind(item: InquiryListItem)
        {
            ongoingBinding.setVariable(BR.ongoingInquiryData, item)
        }
    }

    inner class CompleteViewHolder(binding: InquiryListCompleteItemBinding): RecyclerView.ViewHolder(binding.root)
    {
        val completeBinding: InquiryListCompleteItemBinding = binding

        fun bind(item: InquiryListItem)
        {
            completeBinding.completeInquiryData = item
        }
    }

    private fun <T: ViewDataBinding> bindView(inflater: LayoutInflater, @LayoutRes layoutRes: Int, parent: ViewGroup): T
    {
        return DataBindingUtil.inflate<T>(inflater, layoutRes, parent, false)
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        when (parcelType)
        {
            InquiryItemTypeEnum.Soon ->
            {
                val binding = bindView<InquiryListOngoingItemBinding>(LayoutInflater.from(parent.context), R.layout.inquiry_list_ongoing_item, parent)
                return OngoingViewHolder(binding)
            }
            InquiryItemTypeEnum.Registered ->
            {
                val binding = bindView<InquiryListOngoingItemBinding>(LayoutInflater.from(parent.context), R.layout.inquiry_list_ongoing_item, parent)
                return OngoingViewHolder(binding)
            }
            InquiryItemTypeEnum.Complete ->
            {
                val binding = bindView<InquiryListCompleteItemBinding>(LayoutInflater.from(parent.context), R.layout.inquiry_list_complete_item, parent)
                return CompleteViewHolder(binding)
            }
        }
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val item: InquiryListItem = list[position]

        when (holder)
        {
            is OngoingViewHolder ->
            {
                holder.bind(item)
                holder.itemView.tag = item

                holder.ongoingBinding.tvDeliveryStatus.bringToFront()
                
                if (item.isSelected)
                {
                    setOngoingParcelItemByDelete(holder.ongoingBinding)
                }
                else
                {
                    setOngoingParcelItemByDefault(holder.ongoingBinding)
                }

                // v: View , isRemoveable : Boolean, item : InquiryListItem
                holder.ongoingBinding.cvOngoingParent.setOnClickListener { v ->

                    // 삭제 모드 및 선택되지 않은 택배일 때
                    if (isRemovable && !item.isSelected)
                    {
                        item.isSelected = true
                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
                        setOngoingParcelItemByDelete(holder.ongoingBinding)
                    }
                    else if (isRemovable && item.isSelected)
                    {
                        item.isSelected = false
                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) - 1
                        setOngoingParcelItemByDefault(holder.ongoingBinding)
                    }
                    else
                    {
                        onParcelClickListener?.run { onParcelClicked(view = v, type = 0, parcelId = item.parcelResponse.parcelId) }
                    }
                }

                holder.ongoingBinding.cvOngoingParent.setOnLongClickListener {
                    if(isRemovable) return@setOnLongClickListener true

                    onParcelClickListener?.run { onParcelLongClicked(view = it, type = 0, parcelId = item.parcelResponse.parcelId) }

                    return@setOnLongClickListener true
                }
            }
            is CompleteViewHolder ->
            {
                holder.bind(item)
                holder.itemView.tag = item

                if (item.isSelected)
                {
                    setCompleteParcelItemByDelete(holder.completeBinding)
                }
                else
                {
                    setCompleteParcelItemByDefault(holder.completeBinding)
                }

                holder.completeBinding.cvCompleteParent.setOnClickListener { v ->

                    if (isRemovable && !item.isSelected)
                    {
                        item.isSelected = true
                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
                        setCompleteParcelItemByDelete(holder.completeBinding)
                    }
                    else if (isRemovable && item.isSelected)
                    {
                        item.isSelected = false
                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) - 1
                        setCompleteParcelItemByDefault(holder.completeBinding)
                    }
                    else
                    {
                        onParcelClickListener?.run { onParcelClicked(view = v, type = 1, parcelId = item.parcelResponse.parcelId) }
                    }
                }

                holder.completeBinding.cvCompleteParent.setOnLongClickListener {
                    if(isRemovable) return@setOnLongClickListener true
                    onParcelClickListener?.run { onParcelLongClicked(view = it, type = 0, parcelId = item.parcelResponse.parcelId) }
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun setSelectAll(flag: Boolean)
    {
        if(flag)
        {
            for (item in list)
            {
                if (!item.isSelected)
                {
                    item.isSelected = true
                    cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
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
            cntOfSelectedItemForDelete?.value = 0
            notifyDataSetChanged()
        }
    }

    fun getSelectedListData(): List<Int>
    {
        return list.filter {
            it.isSelected
        }.map {
            it.parcelResponse.parcelId
        }
    }

    private fun setOngoingParcelItemByDelete(binding: InquiryListOngoingItemBinding)
    {
        binding.constraintDeliveryStatusFront.visibility = GONE
        binding.constraintDeliveryStatusBack.visibility = GONE
        binding.constraintDeliveryStatusFrontDelete.visibility = VISIBLE
        binding.constraintDeliveryStatusBackDelete.visibility = VISIBLE
        binding.linearParentListItemRegister.background = ContextCompat.getDrawable(binding.root.context, R.drawable.border_red)
    }

    private fun setOngoingParcelItemByDefault(binding: InquiryListOngoingItemBinding)
    {
        binding.constraintDeliveryStatusFront.visibility = VISIBLE
        binding.constraintDeliveryStatusBack.visibility = VISIBLE
        binding.constraintDeliveryStatusFrontDelete.visibility = GONE
        binding.constraintDeliveryStatusBackDelete.visibility = GONE
        binding.linearParentListItemRegister.background = null
    }

    private fun setCompleteParcelItemByDefault(binding: InquiryListCompleteItemBinding)
    {
        binding.constraintItemPartComplete.visibility = VISIBLE
        binding.constraintDateComplete.visibility = VISIBLE
        binding.vDividerLine.visibility = VISIBLE
        binding.constraintItemPartDeleteComplete.visibility = GONE
        binding.constraintDeliveryStatusFrontComplete.visibility = GONE
        binding.linearItemComplete.background = null
    }

    private fun setCompleteParcelItemByDelete(binding: InquiryListCompleteItemBinding)
    {
        binding.constraintDateComplete.visibility = GONE
        binding.constraintItemPartComplete.visibility = GONE
        binding.vDividerLine.visibility = GONE
        binding.constraintDeliveryStatusFrontComplete.visibility = VISIBLE
        binding.constraintItemPartDeleteComplete.visibility = VISIBLE
        binding.linearItemComplete.background = ContextCompat.getDrawable(binding.root.context, R.drawable.border_red)
    }

    fun changeParcelDeleteMode(flag: Boolean)
    {
        isRemovable = flag

        if(isRemovable)
        {
            return notifyDataSetChanged()
        }

        for (item in list)
        {
            item.isSelected = false
        }

        notifyDataSetChanged()
    }

    //현재는 '배송완료'에만 적용되어있음. 데이터를 무조건 notifyDataSetChanged()로 데이터를 리프레쉬하지 않고 진짜 변경된 데이터만 변경할 수 있도록함.
    fun notifyChanged(updatedList: MutableList<InquiryListItem>)
    {
        updatedList.sortByDescending { it.parcelResponse.arrivalDte }

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
                SopoLog.d("기존 리스트에 해당 index[$index]가 존재하지 않아 list[$index]에 ${updatedList[index].parcelResponse.alias} 아이템을 추가합니다.")
                list.add(updatedList[index])
                notifyIndexList.add(index)
            }
            else if (updatedList[index].parcelResponse.parcelId != list[index].parcelResponse.parcelId)
            {
                SopoLog.d("index[$index]에 해당하는 ${list[index].parcelResponse.alias}와 업데이트될 아이템(${updatedList[index].parcelResponse.alias}) 일치하지 않아 기존 아이템에 업데이트될 아이템을 덮어씁니다.")
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

        this.list = when (parcelType)
        {
            InquiryItemTypeEnum.Soon ->
            {
                list.filter {
                    it.parcelResponse.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE
                }.toMutableList()
            }
            InquiryItemTypeEnum.Registered ->
            {
                list.filter {
                    it.parcelResponse.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE && it.parcelResponse.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE
                }.toMutableList()
            }
            InquiryItemTypeEnum.Complete ->
            {
                list.filter {
                    it.parcelResponse.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE
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
        return when (parcelType)
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