package com.delivery.sopo.models.inquiry

import com.delivery.sopo.database.dto.TimeCountDTO

class InquiryMenuItem(
    val viewType: InquiryMenuType,
    val timeCount: TimeCountDTO? = null,
    val menuTitle: String? = null
){
    enum class InquiryMenuType
    {
        MainMenu, CompleteHistoryList
    }
}