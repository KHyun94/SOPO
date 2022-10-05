/*
package com.delivery.sopo.presentation.views.adapter

import android.view.LayoutInflater
import android.view.View.*
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.DateSelector
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ItemCompletedParcelBinding
import com.delivery.sopo.databinding.ItemSelectorDateBinding
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.InquiryItemTypeEnum
import com.delivery.sopo.enums.InquiryStatusEnum
import com.delivery.sopo.extensions.toEllipsis
import com.delivery.sopo.interfaces.listener.OnParcelClickListener
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.util.setting.DiffCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompletedParcelAdapter(private val mode: Int,  private var parcels: MutableList<InquiryListItem> = mutableListOf()): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private lateinit var parcelClickListener: OnParcelClickListener
    private var isRemoveMode = false

    var updateParcel: String = ""

    fun setOnParcelClickListener(listener: OnParcelClickListener)
    {
        this.parcelClickListener = listener
    }

    inner class DateViewHolder(val binding: ItemSelectorDateBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(date: DateSelector)
        {
            binding.selector = date
            binding.mode = mode
            binding.executePendingBindings()
        }
    }

    inner class ParcelViewHolder(val binding: ItemCompletedParcelBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(item: InquiryListItem) = CoroutineScope(Dispatchers.Main).launch{
            binding.completeInquiryData = item
            binding.executePendingBindings()
        }
    }

    private fun <T: ViewDataBinding> bindView(inflater: LayoutInflater, @LayoutRes layoutRes: Int, parent: ViewGroup): T = DataBindingUtil.inflate<T>(inflater, layoutRes, parent, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return if(viewType == 1)
        {
            val binding = bindView<ItemSelectorDateBinding>(LayoutInflater.from(parent.context), R.layout.item_selector_date, parent)
            DateViewHolder(binding)
        }else
        {
            val binding = bindView<ItemCompletedParcelBinding>(LayoutInflater.from(parent.context), R.layout.item_completed_parcel, parent)
            ParcelViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val item = parcels[position]

        when(holder)
        {
            is DateViewHolder ->
            {

            }
            is ParcelViewHolder ->
            {
                holder.bind(item)

                if(item.isSelected)
                {
                    setCompleteParcelItemByDelete(holder.binding)
                }
                else
                {
                    setCompleteParcelItemByDefault(holder.binding)
                }

                holder.binding.tvCompleteParcelName.text = item.parcel.alias.toEllipsis()

                holder.binding.cvCompleteParent.setOnClickListener { v ->

                    if(isRemoveMode && !item.isSelected)
                    {
                        item.isSelected = true
                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
                        setCompleteParcelItemByDelete(holder.binding)
                    }
                    else if(isRemoveMode && item.isSelected)
                    {
                        item.isSelected = false
                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) - 1
                        setCompleteParcelItemByDefault(holder.binding)
                    }
                    else
                    {
                        parcelClickListener.onEnterParcelDetailClicked(view = v, type = InquiryStatusEnum.COMPLETE, parcelId = item.parcel.parcelId)
                    }
                }

                holder.binding.cvCompleteParent.setOnLongClickListener {
                    if(isRemoveMode) return@setOnLongClickListener true
                    parcelClickListener.onUpdateParcelAliasClicked(view = it, type = InquiryStatusEnum.COMPLETE, parcelId = item.parcel.parcelId)
                    return@setOnLongClickListener true
                }
            }
        }

    }

    fun setSelectAll(flag: Boolean)
    {
        if(flag)
        {
            for(item in parcels)
            {
                if(!item.isSelected)
                {
                    item.isSelected = true
                    cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
                }
            }
            notifyDataSetChanged()
        }
        else
        {
            for(item in parcels)
            {
                item.isSelected = false
            }
            cntOfSelectedItemForDelete?.value = 0
            notifyDataSetChanged()
        }
    }

    fun getSelectedListData(): List<Int>
    {
        return parcels.filter {
            it.isSelected
        }.map {
            it.parcel.parcelId
        }
    }

    private fun setCompleteParcelItemByDefault(binding: ItemCompletedParcelBinding)
    {
        binding.constraintItemPartComplete.visibility = VISIBLE
        binding.constraintDateComplete.visibility = VISIBLE
        binding.vDividerLine.visibility = VISIBLE
        binding.constraintItemPartDeleteComplete.visibility = GONE
        binding.constraintDeliveryStatusFrontComplete.visibility = GONE
        binding.linearItemComplete.background = null
    }

    private fun setCompleteParcelItemByDelete(binding: ItemCompletedParcelBinding)
    {
        binding.constraintDateComplete.visibility = GONE
        binding.constraintItemPartComplete.visibility = GONE
        binding.vDividerLine.visibility = INVISIBLE
        binding.constraintDeliveryStatusFrontComplete.visibility = VISIBLE
        binding.constraintItemPartDeleteComplete.visibility = VISIBLE
        binding.linearItemComplete.background = ContextCompat.getDrawable(binding.root.context, R.drawable.border_all_rounded_11dp_blue)
    }

    fun changeParcelDeleteMode(flag: Boolean)
    {
        isRemoveMode = flag

        if(isRemoveMode)
        {
            return notifyDataSetChanged()
        }

        for(item in parcels)
        {
            item.isSelected = false
        }

        notifyDataSetChanged()
    }

    // 택배 리스트를 상태에 따라 분류
    fun separateDelivered(list: MutableList<InquiryListItem>?)
    {
        if(list == null) return

        val newParcels = list.filter {
            it.parcel.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE
        }.toMutableList()

        val diffCallback = DiffCallback(parcels, newParcels)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        parcels.clear()
        parcels.addAll(newParcels)

        diffResult.dispatchUpdatesTo(this)
    }

    fun separateDeliveryListByStatus(list: MutableList<InquiryListItem>?)
    {
        if(list == null) return

        val newParcels = when(parcelType)
        {
            InquiryItemTypeEnum.Soon ->
            {
                list.filter { it.parcel.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE }
                    .toMutableList()
            }
            InquiryItemTypeEnum.Registered ->
            {
                list.filter { it.parcel.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE && it.parcel.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE }
                    .toMutableList()
            }
            InquiryItemTypeEnum.Complete ->
            {
                list.filter { it.parcel.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE }
                    .toMutableList()
            }
        }

        val diffCallback = DiffCallback(parcels, newParcels)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        parcels.clear()
        parcels.addAll(newParcels)

        diffResult.dispatchUpdatesTo(this@CompletedParcelAdapter)
    }

    override fun getItemCount(): Int = parcels.size
    fun getListSize(): Int = parcels.size

    fun getList(): MutableList<InquiryListItem> = parcels

}
*/
