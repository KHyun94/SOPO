package com.delivery.sopo.models.inquiry

import com.delivery.sopo.database.dto.TimeCountDTO
import com.delivery.sopo.models.entity.TimeCountEntity

class InquiryMenuItem(
    val viewType: InquiryMenuType,
    val timeCount: TimeCountEntity? = null,
    val menuTitle: String? = null
){
    enum class InquiryMenuType
    {
        MainMenu, CompleteHistoryList
    }
}