package com.delivery.sopo.util.setting

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.presentation.views.adapter.AdapterType

class DiffCallback(
        private val oldList: List<InquiryListItem>,
        private val newList: List<InquiryListItem>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].parcel.parcelId == newList[newItemPosition].parcel.parcelId

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}

class ParcelDiffCallback(
    private val oldList: List<AdapterType>,
    private val newList: List<AdapterType>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when
        {
            oldList[oldItemPosition] is AdapterType.Item && newList[newItemPosition] is AdapterType.Item ->
            {
                val oldItem = (oldList[oldItemPosition] as AdapterType.Item)
                val newItem = (newList[newItemPosition] as AdapterType.Item)

                oldItem.item.parcel.parcelId == newItem.item.parcel.parcelId
            }
            oldList[oldItemPosition] is AdapterType.NoItem && newList[newItemPosition] is AdapterType.NoItem ->
            {
                return true
            }
            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when
        {
            oldList[oldItemPosition] is AdapterType.Item && newList[newItemPosition] is AdapterType.Item ->
            {
                val oldItem = (oldList[oldItemPosition] as AdapterType.Item)
                val newItem = (newList[newItemPosition] as AdapterType.Item)

                oldItem.item == newItem.item
            }
            oldList[oldItemPosition] is AdapterType.NoItem && newList[newItemPosition] is AdapterType.NoItem ->
            {
                val oldItem = (oldList[oldItemPosition] as AdapterType.Item)
                val newItem = (newList[newItemPosition] as AdapterType.Item)

                oldItem.item == newItem.item
            }
            else -> false
        }

    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
//        oldList[oldItemPosition] == newList[newItemPosition]
}
