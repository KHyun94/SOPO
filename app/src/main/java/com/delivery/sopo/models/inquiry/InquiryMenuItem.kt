package com.delivery.sopo.models.inquiry

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