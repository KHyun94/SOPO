package com.delivery.sopo.presentation.views.adapter

import android.view.LayoutInflater
import android.view.View.*
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ItemCompletedParcelBinding
import com.delivery.sopo.databinding.ItemOngoingParcelBinding
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

class InquiryListAdapter(private var parcels: MutableList<InquiryListItem> = mutableListOf(), private val parcelType: InquiryItemTypeEnum, private val cntOfSelectedItemForDelete: MutableLiveData<Int>? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private lateinit var parcelClickListener: OnParcelClickListener
    private var isRemoveMode = false

    var updateParcel: String = ""

    fun setOnParcelClickListener(listener: OnParcelClickListener)
    {
        this.parcelClickListener = listener
    }

    inner class OngoingViewHolder(val binding: ItemOngoingParcelBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(item: InquiryListItem)
        {
            binding.setVariable(BR.ongoingInquiryData, item)
            binding.executePendingBindings()
        }

        fun setModifying(){
            Glide.with(binding.ivDeliveryStatus)
                .asGif()
                .load(R.drawable.ic_inquiry_cardview_modifying)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.ivDeliveryStatus)

            binding.tvDeliveryStatus.text = "편집중"
            binding.tvDeliveryStatus.setTextColor(ContextCompat.getColor(binding.tvDeliveryStatus.context, R.color.COLOR_MAIN_700))

            binding.constraintDeliveryStatusFront.setBackgroundResource(R.color.COLOR_MAIN_BLUE_50)
        }
    }

    inner class CompleteViewHolder(val binding: ItemCompletedParcelBinding): RecyclerView.ViewHolder(binding.root)
    {
        fun bind(item: InquiryListItem) = CoroutineScope(Dispatchers.Main).launch{
            binding.completeInquiryData = item
            binding.executePendingBindings()
        }
    }

    private fun <T: ViewDataBinding> bindView(inflater: LayoutInflater, @LayoutRes layoutRes: Int, parent: ViewGroup): T = DataBindingUtil.inflate<T>(inflater, layoutRes, parent, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return when(parcelType)
        {
            InquiryItemTypeEnum.Soon, InquiryItemTypeEnum.Registered ->
            {
                val binding = bindView<ItemOngoingParcelBinding>(LayoutInflater.from(parent.context), R.layout.item_ongoing_parcel, parent)
                OngoingViewHolder(binding)
            }
            InquiryItemTypeEnum.Complete ->
            {
                val binding = bindView<ItemCompletedParcelBinding>(LayoutInflater.from(parent.context), R.layout.item_completed_parcel, parent)
                CompleteViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val item = parcels[position]

        when(holder)
        {
            is OngoingViewHolder ->
            {
                holder.bind(item)

                holder.binding.tvDeliveryStatus.bringToFront()
                holder.binding.tvRegisteredParcelName.text = item.parcel.alias.toEllipsis()

                if(item.isSelected)
                {
                    setOngoingParcelItemByDelete(holder.binding)
                }
                else
                {
                    setOngoingParcelItemByDefault(holder.binding)
                }

                holder.binding.cvOngoingParent.setOnClickListener { v ->

                    when
                    {
                        isRemoveMode && !item.isSelected ->
                        {
                            item.isSelected = true
                            cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
                            setOngoingParcelItemByDelete(holder.binding)
                        }
                        isRemoveMode && item.isSelected ->
                        {
                            item.isSelected = false
                            cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) - 1
                            setOngoingParcelItemByDefault(holder.binding)
                        }
                        else ->
                        {
                            if(item.parcel.deliveryStatus != DeliveryStatusEnum.ORPHANED.CODE)
                            {
                                return@setOnClickListener parcelClickListener.onEnterParcelDetailClicked(view = v, type = InquiryStatusEnum.ONGOING, parcelId = item.parcel.parcelId)
                            }

                            parcelClickListener.onMaintainParcelClicked(view = v, pos = position, parcelId = item.parcel.parcelId)
                        }
                    }
                }

                holder.binding.cvOngoingParent.setOnLongClickListener {
                    if(isRemoveMode) return@setOnLongClickListener true

                    CoroutineScope(Dispatchers.Main).launch { holder.setModifying() }

                    parcelClickListener.onUpdateParcelAliasClicked(view = it, type = InquiryStatusEnum.ONGOING, parcelId = item.parcel.parcelId)

                    return@setOnLongClickListener true
                }

            }
            is CompleteViewHolder ->
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

    private fun setOngoingParcelItemByDelete(binding: ItemOngoingParcelBinding)
    {
        binding.constraintDeliveryStatusFront.visibility = GONE
        binding.constraintDeliveryStatusBack.visibility = GONE
        binding.constraintDeliveryStatusFrontDelete.visibility = VISIBLE
        binding.constraintDeliveryStatusBackDelete.visibility = VISIBLE
        binding.linearParentListItemRegister.background =
            ContextCompat.getDrawable(binding.root.context, R.drawable.border_all_rounded_11dp_blue)
    }

    private fun setOngoingParcelItemByDefault(binding: ItemOngoingParcelBinding)
    {
        binding.constraintDeliveryStatusFront.visibility = VISIBLE
        binding.constraintDeliveryStatusBack.visibility = VISIBLE
        binding.constraintDeliveryStatusFrontDelete.visibility = GONE
        binding.constraintDeliveryStatusBackDelete.visibility = GONE
        binding.linearParentListItemRegister.background = null
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

        diffResult.dispatchUpdatesTo(this@InquiryListAdapter)
    }

    override fun getItemCount(): Int = parcels.size
    fun getListSize(): Int = parcels.size

    fun getList(): MutableList<InquiryListItem> = parcels

}
