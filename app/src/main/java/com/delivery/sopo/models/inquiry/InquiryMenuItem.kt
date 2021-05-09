package com.delivery.sopo.models.inquiry

import com.delivery.sopo.data.repository.database.room.entity.ParcelCntInfoEntity

class InquiryMenuItem(
    val viewType: InquiryMenuType,
    val parcelCntInfo: ParcelCntInfoEntity? = null,
    val menuTitle: String? = null
){
    enum class InquiryMenuType
    {
        MainMenu, CompleteHistoryList
    }
}