package com.delivery.sopo.models.inquiry

import com.delivery.sopo.data.repository.database.room.dto.CompleteParcelStatusDTO
import com.delivery.sopo.data.repository.database.room.entity.CompleteParcelStatusEntity

class InquiryMenuItem(
        val viewType: InquiryMenuType,
        val data: String? = null,
        val menuTitle: String? = null
){
    enum class InquiryMenuType
    {
        MainMenu, CompleteHistoryList
    }
}