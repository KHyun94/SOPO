package com.delivery.sopo.presentation.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ItemCompletedParcelBinding
import com.delivery.sopo.databinding.ItemLabelBinding
import com.delivery.sopo.databinding.ItemOngoingParcelBinding
import com.delivery.sopo.databinding.ItemParcelCntBinding
import com.delivery.sopo.enums.DeliveryStatus
import com.delivery.sopo.enums.InquiryStatus
import com.delivery.sopo.enums.InquiryStatusEnum
import com.delivery.sopo.extensions.toEllipsis
import com.delivery.sopo.extensions.toJson
import com.delivery.sopo.interfaces.listener.OnParcelClickListener
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.setting.DiffCallback
import com.delivery.sopo.util.setting.ParcelDiffCallback
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class AdapterType {
    data class NoItem(val viewType: Int) : AdapterType()
    data class LabelItem(val viewType: Int, val inquiryStatus: String) : AdapterType()
    data class Item(val viewType: Int, val item: InquiryListItem) : AdapterType()
}

class OngoingTypeAdapter(
    private val cntOfSelectedItemForDelete: MutableLiveData<Int>? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPED_PARCEL_COUNT = 0
        const val TYPED_LABEL = 1
        const val TYPED_PARCEL = 2
    }

    private lateinit var parcelClickListener: OnParcelClickListener
    private var isRemoveMode = false

    val adapterItems = mutableListOf<AdapterType>()

    var updateParcel: String = ""

    init {
        adapterItems.add(0, AdapterType.NoItem(TYPED_PARCEL_COUNT))
    }

    fun setOnParcelClickListener(listener: OnParcelClickListener) {
        this.parcelClickListener = listener
    }

    inner class ParcelCountViewHolder(val binding: ItemParcelCntBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.setVariable(BR.parcelCount, 0)
            binding.executePendingBindings()
        }
    }

    inner class OngoingViewHolder(val binding: ItemOngoingParcelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InquiryListItem) {
            binding.setVariable(BR.ongoingInquiryData, item)
            binding.executePendingBindings()
        }
    }

    inner class LabelViewHolder(val binding: ItemLabelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(inquiryStatus: InquiryStatus) {
            binding.inquiryStatus = inquiryStatus
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            TYPED_PARCEL_COUNT -> {
                val binding = ItemParcelCntBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ParcelCountViewHolder(binding)
            }
            TYPED_LABEL -> {
                val binding = ItemLabelBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return LabelViewHolder(binding)
            }
            TYPED_PARCEL -> {
                val binding = ItemOngoingParcelBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )

                return OngoingViewHolder(binding)
            }
            else -> throw Exception("")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val adapterItem = adapterItems[position]) {
            is AdapterType.NoItem -> TYPED_PARCEL_COUNT
            is AdapterType.LabelItem -> TYPED_LABEL
            is AdapterType.Item -> TYPED_PARCEL
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val adapterItem = adapterItems[position]) {
            is AdapterType.NoItem -> {
                (holder as ParcelCountViewHolder).bind()
            }
            is AdapterType.LabelItem -> {
                val inquiryStatus =
                    if (adapterItem.inquiryStatus == DeliveryStatus.OUT_FOR_DELIVERY.CODE) InquiryStatus.Soon else InquiryStatus.Registered
                (holder as LabelViewHolder).bind(inquiryStatus)
            }
            is AdapterType.Item -> {
                val item = adapterItem.item

                (holder as OngoingViewHolder).bind(item)

                holder.binding.tvDeliveryStatus.bringToFront()
                holder.binding.tvRegisteredParcelName.text = item.parcel.alias.toEllipsis()

                if (item.isSelected) {
                    setOngoingParcelItemByDelete(holder.binding)
                } else {
                    setOngoingParcelItemByDefault(holder.binding)
                }

                holder.binding.cvOngoingParent.setOnClickListener { v ->

                    when {
                        isRemoveMode && !item.isSelected -> {
                            item.isSelected = true
                            cntOfSelectedItemForDelete?.value =
                                (cntOfSelectedItemForDelete?.value ?: 0) + 1
                            setOngoingParcelItemByDelete(holder.binding)
                        }
                        isRemoveMode && item.isSelected -> {
                            item.isSelected = false
                            cntOfSelectedItemForDelete?.value =
                                (cntOfSelectedItemForDelete?.value ?: 0) - 1
                            setOngoingParcelItemByDefault(holder.binding)
                        }
                        else -> {
                            if (item.parcel.deliveryStatus != DeliveryStatus.ORPHANED.CODE) {
                                return@setOnClickListener parcelClickListener.onEnterParcelDetailClicked(
                                    view = v,
                                    type = InquiryStatusEnum.ONGOING,
                                    parcelId = item.parcel.parcelId
                                )
                            }

                            parcelClickListener.onMaintainParcelClicked(
                                view = v,
                                pos = position,
                                parcelId = item.parcel.parcelId
                            )
                        }
                    }
                }

                holder.binding.cvOngoingParent.setOnLongClickListener {
                    if (isRemoveMode) return@setOnLongClickListener true

//                    CoroutineScope(Dispatchers.Main).launch { holder.setModifying() }

                    parcelClickListener.onUpdateParcelAliasClicked(
                        view = it,
                        type = InquiryStatusEnum.ONGOING,
                        parcelId = item.parcel.parcelId
                    )

                    return@setOnLongClickListener true
                }
            }
        }

    }

    fun setSelectAll(flag: Boolean) {
        if (flag) {
            for (item in adapterItems) {

                if (item !is AdapterType.Item) continue

                if (!item.item.isSelected) {
                    item.item.isSelected = true
                    cntOfSelectedItemForDelete?.value = (cntOfSelectedItemForDelete?.value ?: 0) + 1
                }
            }
            notifyDataSetChanged()
        } else {

            for (item in adapterItems) {
                if (item !is AdapterType.Item) continue

                item.item.isSelected = false
            }
            cntOfSelectedItemForDelete?.value = 0
            notifyDataSetChanged()
        }
    }

    fun getSelectedListData(): List<Int> {
        return adapterItems.filterIsInstance<AdapterType.Item>().filter {
            it.item.isSelected
        }.map {
            it.item.parcel.parcelId
        }
    }

    private fun setOngoingParcelItemByDelete(binding: ItemOngoingParcelBinding) {
        binding.constraintDeliveryStatusFront.visibility = GONE
        binding.constraintDeliveryStatusBack.visibility = GONE
        binding.constraintDeliveryStatusFrontDelete.visibility = VISIBLE
        binding.constraintDeliveryStatusBackDelete.visibility = VISIBLE
        binding.linearParentListItemRegister.background =
            ContextCompat.getDrawable(binding.root.context, R.drawable.border_all_rounded_11dp_blue)
    }

    private fun setOngoingParcelItemByDefault(binding: ItemOngoingParcelBinding) {
        binding.constraintDeliveryStatusFront.visibility = VISIBLE
        binding.constraintDeliveryStatusBack.visibility = VISIBLE
        binding.constraintDeliveryStatusFrontDelete.visibility = GONE
        binding.constraintDeliveryStatusBackDelete.visibility = GONE
        binding.linearParentListItemRegister.background = null
    }

    private fun setCompleteParcelItemByDefault(binding: ItemCompletedParcelBinding) {
        binding.constraintItemPartComplete.visibility = VISIBLE
        binding.constraintDateComplete.visibility = VISIBLE
        binding.vDividerLine.visibility = VISIBLE
        binding.constraintItemPartDeleteComplete.visibility = GONE
        binding.constraintDeliveryStatusFrontComplete.visibility = GONE
        binding.linearItemComplete.background = null
    }

    private fun setCompleteParcelItemByDelete(binding: ItemCompletedParcelBinding) {
        binding.constraintDateComplete.visibility = GONE
        binding.constraintItemPartComplete.visibility = GONE
        binding.vDividerLine.visibility = INVISIBLE
        binding.constraintDeliveryStatusFrontComplete.visibility = VISIBLE
        binding.constraintItemPartDeleteComplete.visibility = VISIBLE
        binding.linearItemComplete.background =
            ContextCompat.getDrawable(binding.root.context, R.drawable.border_all_rounded_11dp_blue)
    }

    fun changeParcelDeleteMode(flag: Boolean) {
        isRemoveMode = flag

        if (isRemoveMode) {
            return notifyDataSetChanged()
        }

        for (item in adapterItems) {

            if (item !is AdapterType.Item) continue

            item.item.isSelected = false
        }


        notifyDataSetChanged()
    }

    fun separateDeliveryListByStatus(list: MutableList<InquiryListItem>?) {
        if (list == null) return

        val newList = list.map {
            AdapterType.Item(TYPED_PARCEL, it)
        }.toMutableList<AdapterType>()

        val diffCallback = ParcelDiffCallback(adapterItems, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        newList.add(0, AdapterType.NoItem(TYPED_PARCEL_COUNT))

        val soonFirstIndex = newList.indexOfFirst {
            if (it !is AdapterType.Item) return@indexOfFirst false
            it.item.parcel.deliveryStatus == DeliveryStatus.OUT_FOR_DELIVERY.CODE
        }

        Logger.d("index : 1${soonFirstIndex} + ")

        if (soonFirstIndex != -1) {
            val data1 = newList[soonFirstIndex] as AdapterType.Item
            newList.add(
                soonFirstIndex,
                AdapterType.LabelItem(TYPED_LABEL, data1.item.parcel.deliveryStatus)
            )
            Logger.d("index : 1${soonFirstIndex} + ${data1.item.parcel.toString()}")
        }

        val index2 = newList.indexOfFirst {
            if (it !is AdapterType.Item) return@indexOfFirst false

            it.item.parcel.deliveryStatus != DeliveryStatus.OUT_FOR_DELIVERY.CODE
        }

        if (index2 != -1) {
            val data2 = newList[index2] as AdapterType.Item
            newList.add(
                index2,
                AdapterType.LabelItem(TYPED_LABEL, data2.item.parcel.deliveryStatus)
            )
            Logger.d("index : 2 ${index2} + ${data2.item.parcel.toString()}")
        }


        adapterItems.clear()
        adapterItems.addAll(newList)

        diffResult.dispatchUpdatesTo(this@OngoingTypeAdapter)
    }

    override fun getItemCount(): Int = adapterItems.size
    fun getListSize(): Int = adapterItems.size

    fun getList(): MutableList<AdapterType> = adapterItems
}
