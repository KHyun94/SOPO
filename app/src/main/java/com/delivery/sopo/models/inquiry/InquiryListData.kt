package com.delivery.sopo.models.inquiry

import com.delivery.sopo.models.parcel.Parcel


data class InquiryListData(
    val parcel: Parcel,
    var isSelected: Boolean = false
)