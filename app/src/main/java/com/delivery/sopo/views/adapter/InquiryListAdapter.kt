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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.InquiryListCompleteItemBinding
import com.delivery.sopo.databinding.InquiryListOngoingItemBinding
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.InquiryItemTypeEnum
import com.delivery.sopo.enums.InquiryStatusEnum
import com.delivery.sopo.extensions.toEllipsis
import com.delivery.sopo.interfaces.listener.OnParcelClickListener
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.setting.DiffCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InquiryListAdapter(
        private var parcels: MutableList<InquiryListItem> = mutableListOf(),
        private val parcelType: InquiryItemTypeEnum,
        private val cntOfSelectedItemForDelete: MutableLiveData<Int>? = null):
        RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private lateinit var parcelClickListener: OnParcelClickListener
    private var isRemoveMode = false

    fun setOnParcelClickListener(listener: OnParcelClickListener)
    {
        this.parcelClickListener = listener
    }

    inner class OngoingViewHolder(private val binding: InquiryListOngoingItemBinding): RecyclerView.ViewHolder(binding.root)
    {
        val ongoingBinding: InquiryListOngoingItemBinding = binding

        fun bind(item: InquiryListItem)
        {
            ongoingBinding.setVariable(BR.ongoingInquiryData, item)
            ongoingBinding.executePendingBindings()
        }
    }

    inner class CompleteViewHolder(binding: InquiryListCompleteItemBinding): RecyclerView.ViewHolder(binding.root)
    {
        val completeBinding: InquiryListCompleteItemBinding = binding

        fun bind(item: InquiryListItem)
        {
            completeBinding.completeInquiryData = item
            completeBinding.executePendingBindings()
        }
    }

    private fun <T: ViewDataBinding> bindView(inflater: LayoutInflater, @LayoutRes layoutRes: Int, parent: ViewGroup): T =  DataBindingUtil.inflate<T>(inflater, layoutRes, parent, false)

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        when(parcelType)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val item: InquiryListItem = parcels[position]

        when(holder)
        {
            is OngoingViewHolder -> {
                holder.bind(item)
                holder.itemView.tag = item

                CoroutineScope(Dispatchers.Main).launch {
                    holder.ongoingBinding.tvDeliveryStatus.bringToFront()
                    holder.ongoingBinding.tvRegisteredParcelName.text = item.parcel.alias.toEllipsis()
                }

                if(item.isSelected)
                {
                    setOngoingParcelItemByDelete(holder.ongoingBinding)
                }
                else
                {
                    setOngoingParcelItemByDefault(holder.ongoingBinding)
                }

                holder.ongoingBinding.cvOngoingParent.setOnClickListener { v ->

                    // 삭제 모드 및 선택되지 않은 택배일 때
                    if(isRemoveMode && !item.isSelected)
                    {
                        item.isSelected = true
                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
                        setOngoingParcelItemByDelete(holder.ongoingBinding)
                    }
                    else if(isRemoveMode && item.isSelected)
                    {
                        item.isSelected = false
                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) - 1
                        setOngoingParcelItemByDefault(holder.ongoingBinding)
                    }
                    else
                    {
                        if(item.parcel.deliveryStatus != DeliveryStatusEnum.ORPHANED.CODE)
                        {
                            return@setOnClickListener parcelClickListener.onEnterParcelDetailClicked(view = v, type = InquiryStatusEnum.ONGOING, parcelId = item.parcel.parcelId)
                        }

                        parcelClickListener.onMaintainParcelClicked(view = v, pos = position, parcelId = item.parcel.parcelId)
                    }
                }

                holder.ongoingBinding.cvOngoingParent.setOnLongClickListener {
                    if(isRemoveMode) return@setOnLongClickListener true

                    parcelClickListener.onUpdateParcelAliasClicked(view = it, type = InquiryStatusEnum.ONGOING, parcelId = item.parcel.parcelId)

                    return@setOnLongClickListener true
                }
            }
            is CompleteViewHolder ->
            {
                holder.bind(item)
                holder.itemView.tag = item

                if(item.isSelected)
                {
                    setCompleteParcelItemByDelete(holder.completeBinding)
                }
                else
                {
                    setCompleteParcelItemByDefault(holder.completeBinding)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    holder.completeBinding.tvCompleteParcelName.text =item.parcel.alias.toEllipsis()
                }

                holder.completeBinding.cvCompleteParent.setOnClickListener { v ->

                    if(isRemoveMode && !item.isSelected)
                    {
                        item.isSelected = true
                        cntOfSelectedItemForDelete?.value =
                            (cntOfSelectedItemForDelete?.value ?: 0) + 1
                        setCompleteParcelItemByDelete(holder.completeBinding)
                    }
                    else if(isRemoveMode && item.isSelected)
                    {
                        item.isSelected = false
                        cntOfSelectedItemForDelete?.value =
                            (cntOfSelectedItemForDelete?.value ?: 0) - 1
                        setCompleteParcelItemByDefault(holder.completeBinding)
                    }
                    else
                    {
                        parcelClickListener.onEnterParcelDetailClicked(view = v, type = InquiryStatusEnum.COMPLETE, parcelId = item.parcel.parcelId)
                    }
                }

                holder.completeBinding.cvCompleteParent.setOnLongClickListener {
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

    private fun setOngoingParcelItemByDelete(binding: InquiryListOngoingItemBinding)
    {
        binding.constraintDeliveryStatusFront.visibility = GONE
        binding.constraintDeliveryStatusBack.visibility = GONE
        binding.constraintDeliveryStatusFrontDelete.visibility = VISIBLE
        binding.constraintDeliveryStatusBackDelete.visibility = VISIBLE
        binding.linearParentListItemRegister.background = ContextCompat.getDrawable(binding.root.context, R.drawable.border_all_rounded_11dp_blue)
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

    //현재는 '배송완료'에만 적용되어있음. 데이터를 무조건 notifyDataSetChanged()로 데이터를 리프레쉬하지 않고 진짜 변경된 데이터만 변경할 수 있도록함.
    fun notifyChanged(updatedList: MutableList<InquiryListItem>)
    {
        updatedList.sortByDescending { it.parcel.arrivalDte }

        if(parcels.size > updatedList.size)
        {
            parcels.removeIf { parcels.indexOf(it) > updatedList.lastIndex }
            notifyDataSetChanged()
        }

        val notifyIndexList = mutableListOf<Int>()
        for(index in 0..updatedList.lastIndex)
        {
            if(parcels.getOrNull(index) == null)
            {
                SopoLog.d("기존 리스트에 해당 index[$index]가 존재하지 않아 list[$index]에 ${updatedList[index].parcel.alias} 아이템을 추가합니다.")
                parcels.add(updatedList[index])
                notifyIndexList.add(index)
            }
            else if(updatedList[index].parcel.parcelId != parcels[index].parcel.parcelId)
            {
                SopoLog.d("index[$index]에 해당하는 ${parcels[index].parcel.alias}와 업데이트될 아이템(${updatedList[index].parcel.alias}) 일치하지 않아 기존 아이템에 업데이트될 아이템을 덮어씁니다.")
                parcels[index] = updatedList[index]
                notifyIndexList.add(index)
            }
        }

        for(index in 0..notifyIndexList.lastIndex)
        {
            notifyItemChanged(notifyIndexList[index])
        }
    }

    // 택배 리스트를 상태에 따라 분류
    fun separateDelivered(list: MutableList<InquiryListItem>?)
    {
        if(list == null) return

        val newParcels = list.filter {
            it.parcel.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE
        }.toMutableList()

        val diffCallback = DiffCallback(parcels,  newParcels)
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
                list.filter {
                    it.parcel.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE
                }.toMutableList()
            }
            InquiryItemTypeEnum.Registered ->
            {
                list.filter {
                    it.parcel.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE && it.parcel.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE
                }.toMutableList()
            }
            InquiryItemTypeEnum.Complete ->
            {
                list.filter {
                    it.parcel.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE
                }.toMutableList()
            }
        }

        val diffCallback = DiffCallback(parcels, newParcels)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        parcels.clear()
        parcels.addAll(newParcels)

        diffResult.dispatchUpdatesTo(this)

    }

    override fun getItemCount(): Int =  parcels.size
    fun getListSize(): Int = parcels.size

    fun getList(): MutableList<InquiryListItem> = parcels

}

//class InquiryListAdapter(
//        private var parcels: MutableList<InquiryListItem> = mutableListOf(),
//        private val parcelType: InquiryItemTypeEnum,
//        private val cntOfSelectedItemForDelete: MutableLiveData<Int>? = null):
//        RecyclerView.Adapter<RecyclerView.ViewHolder>()
//{
//    private lateinit var parcelClickListener: OnParcelClickListener
//    private var isRemoveMode = false
//
//    fun setOnParcelClickListener(listener: OnParcelClickListener)
//    {
//        this.parcelClickListener = listener
//    }
//
//    inner class OngoingViewHolder(private val binding: InquiryListOngoingItemBinding): RecyclerView.ViewHolder(binding.root)
//    {
//        val ongoingBinding: InquiryListOngoingItemBinding = binding
//
//        fun bind(item: InquiryListItem)
//        {
//            ongoingBinding.setVariable(BR.ongoingInquiryData, item)
//            ongoingBinding.executePendingBindings()
//        }
//    }
//
//    inner class CompleteViewHolder(binding: InquiryListCompleteItemBinding): RecyclerView.ViewHolder(binding.root)
//    {
//        val completeBinding: InquiryListCompleteItemBinding = binding
//
//        fun bind(item: InquiryListItem)
//        {
//            completeBinding.completeInquiryData = item
//            completeBinding.executePendingBindings()
//        }
//    }
//
//    private fun <T: ViewDataBinding> bindView(inflater: LayoutInflater, @LayoutRes layoutRes: Int, parent: ViewGroup): T =  DataBindingUtil.inflate<T>(inflater, layoutRes, parent, false)
//
//    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
//    {
//        when(parcelType)
//        {
//            InquiryItemTypeEnum.Soon ->
//            {
//                val binding = bindView<InquiryListOngoingItemBinding>(LayoutInflater.from(parent.context), R.layout.inquiry_list_ongoing_item, parent)
//                return OngoingViewHolder(binding)
//            }
//            InquiryItemTypeEnum.Registered ->
//            {
//                val binding = bindView<InquiryListOngoingItemBinding>(LayoutInflater.from(parent.context), R.layout.inquiry_list_ongoing_item, parent)
//                return OngoingViewHolder(binding)
//            }
//            InquiryItemTypeEnum.Complete ->
//            {
//                val binding = bindView<InquiryListCompleteItemBinding>(LayoutInflater.from(parent.context), R.layout.inquiry_list_complete_item, parent)
//                return CompleteViewHolder(binding)
//            }
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
//    {
//        val item: InquiryListItem = parcels[position]
//
//        when(holder)
//        {
//            is OngoingViewHolder -> {
//                holder.bind(item)
//                holder.itemView.tag = item
//
//                CoroutineScope(Dispatchers.Main).launch {
//                    holder.ongoingBinding.tvDeliveryStatus.bringToFront()
//                    holder.ongoingBinding.tvRegisteredParcelName.text = item.parcel.alias.toEllipsis()
//                }
//
//                if(item.isSelected)
//                {
//                    setOngoingParcelItemByDelete(holder.ongoingBinding)
//                }
//                else
//                {
//                    setOngoingParcelItemByDefault(holder.ongoingBinding)
//                }
//
//                holder.ongoingBinding.cvOngoingParent.setOnClickListener { v ->
//
//                    // 삭제 모드 및 선택되지 않은 택배일 때
//                    if(isRemoveMode && !item.isSelected)
//                    {
//                        item.isSelected = true
//                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
//                        setOngoingParcelItemByDelete(holder.ongoingBinding)
//                    }
//                    else if(isRemoveMode && item.isSelected)
//                    {
//                        item.isSelected = false
//                        cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) - 1
//                        setOngoingParcelItemByDefault(holder.ongoingBinding)
//                    }
//                    else
//                    {
//                        if(item.parcel.deliveryStatus != DeliveryStatusEnum.ORPHANED.CODE)
//                        {
//                            return@setOnClickListener parcelClickListener.onEnterParcelDetailClicked(view = v, type = InquiryStatusEnum.ONGOING, parcelId = item.parcel.parcelId)
//                        }
//
//                        parcelClickListener.onMaintainParcelClicked(view = v, pos = position, parcelId = item.parcel.parcelId)
//                    }
//                }
//
//                holder.ongoingBinding.cvOngoingParent.setOnLongClickListener {
//                    if(isRemoveMode) return@setOnLongClickListener true
//
//                    parcelClickListener.onUpdateParcelAliasClicked(view = it, type = InquiryStatusEnum.ONGOING, parcelId = item.parcel.parcelId)
//
//                    return@setOnLongClickListener true
//                }
//            }
//            is CompleteViewHolder ->
//            {
//                holder.bind(item)
//                holder.itemView.tag = item
//
//                if(item.isSelected)
//                {
//                    setCompleteParcelItemByDelete(holder.completeBinding)
//                }
//                else
//                {
//                    setCompleteParcelItemByDefault(holder.completeBinding)
//                }
//
//                CoroutineScope(Dispatchers.Main).launch {
//                    holder.completeBinding.tvCompleteParcelName.text =item.parcel.alias.toEllipsis()
//                }
//
//                holder.completeBinding.cvCompleteParent.setOnClickListener { v ->
//
//                    if(isRemoveMode && !item.isSelected)
//                    {
//                        item.isSelected = true
//                        cntOfSelectedItemForDelete?.value =
//                            (cntOfSelectedItemForDelete?.value ?: 0) + 1
//                        setCompleteParcelItemByDelete(holder.completeBinding)
//                    }
//                    else if(isRemoveMode && item.isSelected)
//                    {
//                        item.isSelected = false
//                        cntOfSelectedItemForDelete?.value =
//                            (cntOfSelectedItemForDelete?.value ?: 0) - 1
//                        setCompleteParcelItemByDefault(holder.completeBinding)
//                    }
//                    else
//                    {
//                        parcelClickListener.onEnterParcelDetailClicked(view = v, type = InquiryStatusEnum.COMPLETE, parcelId = item.parcel.parcelId)
//                    }
//                }
//
//                holder.completeBinding.cvCompleteParent.setOnLongClickListener {
//                    if(isRemoveMode) return@setOnLongClickListener true
//                    parcelClickListener.onUpdateParcelAliasClicked(view = it, type = InquiryStatusEnum.COMPLETE, parcelId = item.parcel.parcelId)
//                    return@setOnLongClickListener true
//                }
//            }
//        }
//    }
//
//
//    fun setSelectAll(flag: Boolean)
//    {
//        if(flag)
//        {
//            for(item in parcels)
//            {
//                if(!item.isSelected)
//                {
//                    item.isSelected = true
//                    cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
//                }
//            }
//            notifyDataSetChanged()
//        }
//        else
//        {
//            for(item in parcels)
//            {
//                item.isSelected = false
//            }
//            cntOfSelectedItemForDelete?.value = 0
//            notifyDataSetChanged()
//        }
//    }
//
//    fun getSelectedListData(): List<Int>
//    {
//        return parcels.filter {
//            it.isSelected
//        }.map {
//            it.parcel.parcelId
//        }
//    }
//
//    private fun setOngoingParcelItemByDelete(binding: InquiryListOngoingItemBinding)
//    {
//        binding.constraintDeliveryStatusFront.visibility = GONE
//        binding.constraintDeliveryStatusBack.visibility = GONE
//        binding.constraintDeliveryStatusFrontDelete.visibility = VISIBLE
//        binding.constraintDeliveryStatusBackDelete.visibility = VISIBLE
//        binding.linearParentListItemRegister.background = ContextCompat.getDrawable(binding.root.context, R.drawable.border_all_rounded_11dp_blue)
//    }
//
//    private fun setOngoingParcelItemByDefault(binding: InquiryListOngoingItemBinding)
//    {
//        binding.constraintDeliveryStatusFront.visibility = VISIBLE
//        binding.constraintDeliveryStatusBack.visibility = VISIBLE
//        binding.constraintDeliveryStatusFrontDelete.visibility = GONE
//        binding.constraintDeliveryStatusBackDelete.visibility = GONE
//        binding.linearParentListItemRegister.background = null
//    }
//
//    private fun setCompleteParcelItemByDefault(binding: InquiryListCompleteItemBinding)
//    {
//        binding.constraintItemPartComplete.visibility = VISIBLE
//        binding.constraintDateComplete.visibility = VISIBLE
//        binding.vDividerLine.visibility = VISIBLE
//        binding.constraintItemPartDeleteComplete.visibility = GONE
//        binding.constraintDeliveryStatusFrontComplete.visibility = GONE
//        binding.linearItemComplete.background = null
//    }
//
//    private fun setCompleteParcelItemByDelete(binding: InquiryListCompleteItemBinding)
//    {
//        binding.constraintDateComplete.visibility = GONE
//        binding.constraintItemPartComplete.visibility = GONE
//        binding.vDividerLine.visibility = GONE
//        binding.constraintDeliveryStatusFrontComplete.visibility = VISIBLE
//        binding.constraintItemPartDeleteComplete.visibility = VISIBLE
//        binding.linearItemComplete.background = ContextCompat.getDrawable(binding.root.context, R.drawable.border_all_rounded_11dp_blue)
//    }
//
//    fun changeParcelDeleteMode(flag: Boolean)
//    {
//        isRemoveMode = flag
//
//        if(isRemoveMode)
//        {
//            return notifyDataSetChanged()
//        }
//
//        for(item in parcels)
//        {
//            item.isSelected = false
//        }
//
//        notifyDataSetChanged()
//    }
//
// /*   //현재는 '배송완료'에만 적용되어있음. 데이터를 무조건 notifyDataSetChanged()로 데이터를 리프레쉬하지 않고 진짜 변경된 데이터만 변경할 수 있도록함.
//    fun notifyChanged(updatedList: MutableList<InquiryListItem>)
//    {
//        updatedList.sortByDescending { it.parcel.arrivalDte }
//
//        if(parcels.size > updatedList.size)
//        {
//            parcels.removeIf { parcels.indexOf(it) > updatedList.lastIndex }
//            notifyDataSetChanged()
//        }
//
//        val notifyIndexList = mutableListOf<Int>()
//        for(index in 0..updatedList.lastIndex)
//        {
//            if(parcels.getOrNull(index) == null)
//            {
//                SopoLog.d("기존 리스트에 해당 index[$index]가 존재하지 않아 list[$index]에 ${updatedList[index].parcel.alias} 아이템을 추가합니다.")
//                parcels.add(updatedList[index])
//                notifyIndexList.add(index)
//            }
//            else if(updatedList[index].parcel.parcelId != parcels[index].parcel.parcelId)
//            {
//                SopoLog.d("index[$index]에 해당하는 ${parcels[index].parcel.alias}와 업데이트될 아이템(${updatedList[index].parcel.alias}) 일치하지 않아 기존 아이템에 업데이트될 아이템을 덮어씁니다.")
//                parcels[index] = updatedList[index]
//                notifyIndexList.add(index)
//            }
//        }
//
//        for(index in 0..notifyIndexList.lastIndex)
//        {
//            notifyItemChanged(notifyIndexList[index])
//        }
//    }*/
//
//    fun separateDeliveryListByStatus(parcels: MutableList<InquiryListItem>){
//        val newParcels = parcels.filter { it.parcel.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE }
//        dispatchUpdateItems(parcels, newParcels)
//    }
//
//    fun separateSoonParcels(parcels: MutableList<InquiryListItem>){
//        val newParcels = parcels.filter { it.parcel.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE }
//        dispatchUpdateItems(parcels, newParcels)
//    }
//
//    fun separateRegisteredParcels(parcels: MutableList<InquiryListItem>){
//        val newParcels = parcels.filter { it.parcel.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE && it.parcel.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE }
//        dispatchUpdateItems(parcels, newParcels)
//    }
//
//    private fun dispatchUpdateItems(oldItems:List<InquiryListItem>, newItems:List<InquiryListItem>){
//
//        oldItems.forEach {
//            SopoLog.d("Old Parcel ${it.parcel.toString()}")
//        }
//
//        oldItems.forEach {
//            SopoLog.d("Old Parcel ${it.parcel.toString()}")
//        }
//
//
//        val diffCallback = DiffCallback(oldItems, newItems)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//
//        parcels.clear()
//        parcels.addAll(newItems)
//
//        diffResult.dispatchUpdatesTo(this)
//    }
//
//    override fun getItemCount(): Int =  parcels.size
//    fun getListSize(): Int = parcels.size
//
//    fun getList(): MutableList<InquiryListItem> = parcels
//
//}