package com.delivery.sopo.util.setting

import androidx.recyclerview.widget.DiffUtil
import com.delivery.sopo.models.inquiry.InquiryListItem

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